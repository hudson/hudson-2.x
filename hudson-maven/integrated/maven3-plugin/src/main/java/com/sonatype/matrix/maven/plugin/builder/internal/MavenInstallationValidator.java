/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.internal;

import org.sonatype.gossip.support.MuxLoggerFactory;
import com.sonatype.matrix.maven.plugin.install.MavenInstallation;
import com.sonatype.matrix.maven.plugin.install.SlaveBundleInstaller;
import com.sonatype.matrix.tasks.Chmod;
import com.sonatype.matrix.tasks.FetchClassLocation;
import com.sonatype.matrix.tasks.TaskListenerLogger;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.ArgumentListBuilder;
import hudson.util.ClasspathBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.util.version.GenericVersionScheme;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionConstraint;
import org.sonatype.aether.version.VersionScheme;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sonatype.matrix.maven.plugin.builder.internal.MavenConstants.JAVA_HOME;
import static com.sonatype.matrix.maven.plugin.builder.internal.MavenConstants.M2_HOME;
import static com.sonatype.matrix.maven.plugin.builder.internal.MavenConstants.MAVEN_OPTS;
import static com.sonatype.matrix.maven.plugin.builder.internal.MavenConstants.MAVEN_SKIP_RC;
import static com.sonatype.matrix.maven.plugin.builder.internal.MavenConstants.TRUE;
import static java.lang.String.format;

/**
 * Perform validation of a Maven installation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public class MavenInstallationValidator
{
    private static final Logger log = LoggerFactory.getLogger(MavenInstallationValidator.class);

    private final MavenInstallation installation;

    private final AbstractBuild<?, ?> build;

    private final EnvVars buildEnv;

    private final Launcher launcher;

    private final BuildListener listener;

    private final TaskListenerLogger logger;

    private final Logger muxlog;

    public MavenInstallationValidator(final MavenInstallation installation,
                                      final AbstractBuild<?, ?> build,
                                      final EnvVars buildEnv,
                                      final Launcher launcher,
                                      final BuildListener listener)
    {
        this.installation = checkNotNull(installation);
        this.build = checkNotNull(build);
        this.buildEnv = checkNotNull(buildEnv);
        this.launcher = checkNotNull(launcher);
        this.listener = checkNotNull(listener);
        this.logger = new TaskListenerLogger(listener);
        this.muxlog = MuxLoggerFactory.create(log, logger);
    }

    private FilePath home;

    public FilePath getHome() {
        if (home == null) {
            home = new FilePath(launcher.getChannel(), installation.getHome());
        }
        return home;
    }

    private void ensureFileExists(final FilePath file) throws Exception {
        if (!file.exists()) {
            throw new AbortException(format("Missing required file: %s", file));
        }
    }

    private FilePath executable;

    public FilePath getExecutable() throws Exception {
        if (executable == null) {
            String file = "mvn";
            if (!launcher.isUnix()) {
                file += ".bat";
            }

            FilePath dir = getHome();
            FilePath mvn = dir.child("bin").child(file);
            ensureFileExists(mvn);

            // Make sure its executable
            if (launcher.isUnix()) {
                // noinspection OctalInteger
                mvn.act(new Chmod(0755));
            }

            executable = mvn;
        }

        return executable;
    }

    private String mavenVersion;

    private static final long timeout = 60;

    private static final TimeUnit timeoutUnit = TimeUnit.SECONDS;

    private String getMavenVersion() throws Exception {
        if (mavenVersion == null) {
            muxlog.info("Checking Maven installation version");

            ArgumentListBuilder args = new ArgumentListBuilder();
            args.add(getExecutable());
            args.add("--version");

            EnvVars env = new EnvVars();
            maybyPut(JAVA_HOME, buildEnv, env);
            env.put(M2_HOME, getHome().getRemote());
            env.put(MAVEN_SKIP_RC, TRUE);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            Proc process = launcher.launch()
                .cmds(args)
                .envs(env)
                .pwd(build.getWorkspace())
                .stdout(buffer)
                .start();

            int result = process.joinWithTimeout(timeout, timeoutUnit, listener);

            String output = new String(buffer.toByteArray());
            if (muxlog.isTraceEnabled()) {
                muxlog.trace("Process output:\n{}", output);
            }

            if (result != 0) {
                throw new AbortException(
                    format("Failed to determine Maven installation version; unexpected exit code: %d, command output: %s", process.join(), output));
            }

            BufferedReader reader = new BufferedReader(new StringReader(output));
            mavenVersion = new MavenVersionParser().parse(reader);
            if (mavenVersion == null) {
                throw new AbortException(format("Failed to determine Maven installation version; unable to parse version from: %s", output));
            }

            muxlog.info("Detected Maven installation version: {}", mavenVersion);
        }
        return mavenVersion;
    }

    private void maybyPut(final String key, final EnvVars source, final EnvVars target) {
        String value = source.get(key);
        if (value != null) {
            target.put(key, value);
        }
    }

    public String getEventSpyVersion() throws Exception {
        // FIXME: This should probably be in its own component, as well as most of the validation bits
        VersionScheme versionScheme = new GenericVersionScheme();
        VersionConstraint versionConstraint = new GenericVersionScheme().parseVersionConstraint("[3.0.3,)");
        Version version;

        String tmp = getMavenVersion();
        try {
            version = versionScheme.parseVersion(tmp);
        }
        catch (Exception e) {
            throw new AbortException("Unable to parse Maven version: " + tmp);
        }

        // FIXME: For now we only have one spy version, so just make sure that the mvn version given is compatible
        if (versionConstraint.containsVersion(version)) {
            return "3.0";
        }

        throw new AbortException("Unsupported Maven version: " + version);
    }

    /**
     * Runs mvn --help and sets MAVEN_OPTS to some garbage and expects the execution to fail.
     * This is only advisory as if the script fails before we get to executing the JVM then we can't
     * really know if the installation is valid or not.
     *
     * @throws Exception
     */
    public void validate() throws Exception {
        muxlog.info("Checking Maven installation environment");

        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(getExecutable());
        args.add("--help");

        EnvVars env = new EnvVars();
        maybyPut(JAVA_HOME, buildEnv, env);
        env.put(M2_HOME, getHome().getRemote());
        env.put(MAVEN_SKIP_RC, TRUE);
        env.put(MAVEN_OPTS, "--no-such-option");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Proc process = launcher.launch()
            .cmds(args)
            .envs(env)
            .pwd(build.getWorkspace())
            .stdout(buffer)
            .start();

        int result = process.joinWithTimeout(timeout, timeoutUnit, listener);

        String output = new String(buffer.toByteArray());
        if (muxlog.isTraceEnabled()) {
            muxlog.trace("Process output:\n{}", output);
        }

        if (result == 0) {
            throw new AbortException("Invalid Maven installation environment; unable to configure MAVEN_OPTS");
        }

        // TODO: Launch something close to what needs to be run, using a special spy to help us make sure it works
        // TODO: Set DELEGATE_PROPERTY to com.sonatype.matrix.maven.eventspy.TerminateJvmEventSpy and expect exit status of 99
        // TODO: Use -N, bogus-goal and/or other options to prevent mvn from doing real work in case that we have an unsupported version
        // TODO: Could use the same execution to detect use of MAVEN_OPTS and determine eventspy compatibility
    }

    private ClasspathBuilder extClasspath;

    public ClasspathBuilder getExtClasspath() throws Exception {
        if (extClasspath == null) {
            ClasspathBuilder cp = new ClasspathBuilder();

            FilePath root = SlaveBundleInstaller.getInstallRoot();
            FilePath file;

            // Include resources dir for configuration files, like logback.xml
            file = root.child("resources");
            ensureFileExists(file);
            cp.add(file);

            file = root.child("lib").child(format("matrix-maven-eventspy-%s.jar", getEventSpyVersion()));
            ensureFileExists(file);
            cp.add(file);

            // Include the remoting jar separately, its not included in the slavebundle
            file = launcher.getChannel().call(new FetchClassLocation(hudson.remoting.Launcher.class));
            ensureFileExists(file);
            cp.add(file);

            extClasspath = cp;
        }

        return extClasspath;
    }
}