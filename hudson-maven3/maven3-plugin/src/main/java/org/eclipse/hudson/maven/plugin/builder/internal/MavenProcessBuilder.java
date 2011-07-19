/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.maven.plugin.builder.internal;

import org.eclipse.hudson.utils.common.TestAccessible;
import org.eclipse.hudson.maven.model.PropertiesDTO;
import org.eclipse.hudson.maven.model.config.BuildConfigurationDTO;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.util.ArgumentListBuilder;
import hudson.util.ClasspathBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.hudson.maven.model.ModelUtil.isEmpty;
import static org.eclipse.hudson.maven.model.ModelUtil.isSet;
import static org.eclipse.hudson.maven.plugin.builder.internal.MavenConstants.*;
import static org.eclipse.hudson.maven.plugin.builder.internal.PathNormalizer.Platform.UNIX;
import static org.eclipse.hudson.maven.plugin.builder.internal.PathNormalizer.Platform.WINDOWS;
import static org.model.hudson.maven.eventspy.common.Constants.PORT_PROPERTY;

/**
 * Helper to build the arguments and environment for launching Maven.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class MavenProcessBuilder
{
    private final Logger log;

    private boolean windows;

    private BuildConfigurationDTO config;

    private FilePath tmpDir;

    private FilePath mavenHome;

    private FilePath executable;

    private ClasspathBuilder extClasspath;

    private FilePath repository;

    private EnvVars env;

    private Map<String, String> buildVariables;

    private Integer port;

    private FilePath workingDir;

    private OutputStream standardOutput;

    public MavenProcessBuilder(final Logger log) {
        this.log = checkNotNull(log);
    }

    @TestAccessible
    MavenProcessBuilder() {
        this.log = LoggerFactory.getLogger(getClass());
    }

    public MavenProcessBuilder withWindows(final boolean flag) {
        this.windows = flag;
        return this;
    }

    public MavenProcessBuilder withTmpDir(final FilePath dir) {
        this.tmpDir = dir;
        return this;
    }

    public MavenProcessBuilder withMavenHome(final FilePath dir) {
        this.mavenHome = dir;
        return this;
    }

    public MavenProcessBuilder withMavenExecutable(final FilePath file) {
        this.executable = file;
        return this;
    }

    public MavenProcessBuilder withExtClasspath(final ClasspathBuilder classpath) {
        this.extClasspath = classpath;
        return this;
    }

    public MavenProcessBuilder withRepository(final FilePath dir) {
        this.repository = dir;
        return this;
    }

    public MavenProcessBuilder withBuildVariables(final Map<String, String> variables) {
        this.buildVariables = variables;
        return this;
    }

    public MavenProcessBuilder withEnv(final EnvVars env) {
        this.env = env;
        return this;
    }

    public MavenProcessBuilder withPort(final int port) {
        this.port = port;
        return this;
    }

    public MavenProcessBuilder withConfiguration(final BuildConfigurationDTO config) {
        this.config = config;
        return this;
    }

    public MavenProcessBuilder withWorkingDirectory(final FilePath dir) {
        this.workingDir = dir;
        return this;
    }

    public MavenProcessBuilder withStandardOutput(final OutputStream output) {
        this.standardOutput = output;
        return this;
    }

    private <T> T ensureNotNull(final T obj) {
        if (obj == null) {
            throw new IllegalStateException();
        }
        return obj;
    }

    /**
     * Allows sub-class to perform variable resolution.  All textual user-inputs should pass though this method.
     */
    protected String resolve(final String value) {
        return value;
    }

    private PathNormalizer normalizer;

    private PathNormalizer getNormalizer() {
        if (normalizer == null) {
            normalizer = new PathNormalizer(windows ? WINDOWS : UNIX);
        }
        return normalizer;
    }

    private String normalize(final String path) {
        return getNormalizer().normalize(path);
    }

    private FilePath normalize(final FilePath path) {
        return getNormalizer().normalize(path);
    }

    private ClasspathBuilder normalize(final ClasspathBuilder path) {
        return getNormalizer().normalize(path);
    }

    /**
     * Build the arguments.
     */
    public ArgumentListBuilder buildArguments() throws Exception {
        ensureNotNull(config);

        ArgumentListBuilder args = new ArgumentListBuilder();

        args.add(normalize(ensureNotNull(executable)));

        if (!isEmpty(config.getGoals())) {
            String goals = resolve(config.getGoals());
            args.addTokenized(goals);
            detectSuperfluousOptions(args);
            if (detectBannedOptions(args)) {
                // Do not attempt to go any further if we found banned options, the mvn execution won't work as expected
                throw new AbortException("Detected banned options");
            }
        }

        // Always show the version being used.
        args.add("-V");

        if (isSet(config.getErrors())) {
            args.add("-e");
        }

        // FIXME: This does not properly resolve the values or trim them for spaces
        args.addKeyValuePairs("-D", ensureNotNull(buildVariables));

        if (config.getProperties() != null) {
            for (PropertiesDTO.Entry entry : config.getProperties().getEntries()) {
                String value = resolve(entry.getValue());
                value = value.trim();
                args.add(String.format("-D%s=%s", entry.getName(), value));
            }
        }

        // Just warn about banned properties, don't puke (may be setting some debug bits)
        detectBannedProperties(args);

        // Add eventspy configuration, needs to be here to avoid problems with spaces in paths
        args.add(String.format("-D%s=%s", MAVEN_EXT_CLASS_PATH, normalize(ensureNotNull(extClasspath)).toString(windows ? ";" : ":")));
        args.add(String.format("-D%s=%d", PORT_PROPERTY, ensureNotNull(port)));

        if (isSet(config.getPrivateRepository())) {
            // Complain when private repo configured as well as custom repository location via properties
            if (config.getProperties() != null && config.getProperties().contains(MAVEN_REPO_LOCAL)) {
                log.warn("Private repository configured as well as custom '{}' property; Custom property will take precedence", MAVEN_REPO_LOCAL);
            }
            else {
                args.add(String.format("-D%s=%s", MAVEN_REPO_LOCAL, normalize(ensureNotNull(repository))));
            }
        }

        if (!isEmpty(config.getPomFile())) {
            String file = resolve(config.getPomFile());
            args.add("-f").add(file);
        }

        if (isSet(config.getOffline())) {
            args.add("-o");
        }

        if (!isSet(config.getRecursive())) {
            args.add("-N");
        }

        if (config.getProfiles() != null) {
            for (String profile : config.getProfiles()) {
                profile = resolve(profile);
                args.add("-P").add(profile);
            }
        }

        if (config.getProjects() != null) {
            for (String project : config.getProjects()) {
                project = resolve(project);
                args.add("-pl").add(project);
            }
        }

        if (!isEmpty(config.getResumeFrom())) {
            String resumeFrom = resolve(config.getResumeFrom());
            args.add("-rf").add(resumeFrom);
        }

        if (config.getVerbosity() != null) {
            switch (config.getVerbosity()) {
                case QUIET:
                    args.add("-q");
                    break;
                case DEBUG:
                    args.add("-X");
                    break;
            }
        }

        if (config.getChecksumMode() != null) {
            switch (config.getChecksumMode()) {
                case LAX:
                    args.add("-c");
                    break;
                case STRICT:
                    args.add("-C");
            }
        }

        if (config.getFailMode() != null) {
            switch (config.getFailMode()) {
                case FAST:
                    args.add("-ff");
                    break;
                case AT_END:
                    args.add("-fae");
                    break;
                case NEVER:
                    args.add("-fn");
                    break;
            }
        }

        if (config.getMakeMode() != null) {
            switch (config.getMakeMode()) {
                case DEPENDENCIES:
                    args.add("-am");
                    break;
                case DEPENDENTS:
                    args.add("-amd");
                    break;
                case BOTH:
                    args.add("-am").add("-amd");
                    break;
            }
        }

        if (config.getSnapshotUpdateMode() != null) {
            switch (config.getSnapshotUpdateMode()) {
                case FORCE:
                    args.add("-U");
                    break;
                case SUPPRESS:
                    args.add("-nsu");
                    break;
            }
        }

        if (!isEmpty(config.getThreading())) {
            String threading = resolve(config.getThreading());
            args.add("-T").add(threading);
        }

        if (log.isDebugEnabled()) {
            log.debug("Arguments:");
            for (String arg : args.toCommandArray()) {
                log.debug("  '{}'", arg);
            }
        }

        return args;
    }

    /**
     * Build MAVEN_OPTS values.
     */
    public ArgumentListBuilder buildOpts() throws Exception {
        ensureNotNull(config);

        ArgumentListBuilder mavenOpts = new ArgumentListBuilder();

        if (!isEmpty(config.getMavenOpts())) {
            String opts = resolve(config.getMavenOpts());
            mavenOpts.addTokenized(opts);
            detectSuperfluousOptions(mavenOpts);
            detectBannedProperties(mavenOpts);
            if (detectBannedOptions(mavenOpts)) {
                // Do not attempt to go any further if we found banned options, the mvn execution won't work as expected
                throw new AbortException("Detected banned options");
            }
        }

        if (isSet(config.getPrivateTmpdir())) {
            // Add tmpdir if not already configured
            String customTmpdir = findArgumentWithPrefix(mavenOpts, String.format("-D%s=", JAVA_IO_TMPDIR));
            if (customTmpdir != null) {
                log.warn("Using user-configured tmpdir: {}", customTmpdir);
            }
            else {
                if (tmpDir != null) {
                    String path = tmpDir.getRemote();
                    if (path.contains(" ")) {
                        log.warn("Path contains spaces: {}", path);
                    }

                    // Ensure that the path has a trailing separator char, some bits look like they expect it (hsperfdata* tmpdir)
                    String sep = windows ? "\\" : "/";
                    if (!path.endsWith(sep)) {
                        path += sep;
                    }

                    mavenOpts.add(String.format("-D%s=%s", JAVA_IO_TMPDIR, normalize(path)));
                }
                else {
                    log.warn("Using default tmpdir");
                }
            }
        }

        return mavenOpts;
    }

    private String findArgumentWithPrefix(final ArgumentListBuilder args, final String prefix) {
        for (String arg : args.toList()) {
            if (arg.startsWith(prefix)) {
                return arg;
            }
        }
        return null;
    }

    /**
     * Build the environment.
     */
    public EnvVars buildEnv() throws Exception {
        ensureNotNull(env);

        // These are for windows only
        if (windows) {
            env.put(MAVEN_TERMINATE_CMD, ON);
            env.put(MAVEN_BATCH_ECHO, OFF);
            env.put(MAVEN_BATCH_PAUSE, OFF);
        }

        env.put(M2_HOME, normalize(ensureNotNull(mavenHome)).getRemote());
        env.put(MAVEN_OPTS, buildOpts().toStringWithQuote());
        env.put(MAVEN_SKIP_RC, TRUE);

        if (log.isDebugEnabled()) {
            log.debug("Environment:");
            for (Map.Entry<String, String> entry : env.entrySet()) {
                log.debug("  {}='{}'", entry.getKey(), entry.getValue());
            }
        }

        return env;
    }

    /**
     * Build the process starter.
     */
    public Launcher.ProcStarter build(final Launcher.ProcStarter starter) throws Exception {
        assert starter != null;

        return starter
            .cmds(buildArguments())
            .envs(buildEnv())
            .pwd(ensureNotNull(workingDir))
            .stdout(ensureNotNull(standardOutput));
    }

    // FIXME: Due to the use of commons-cli which does not require the fully qualified option name to be used,
    // FIXME: ... these checks will fail to catch some uses of options.  May need to either use commons-cli here
    // FIXME: ... to validate or figure out what the unique prefix for each is and check for arg startsWith()

    /**
     * Superfluous options, users should be warned when configuring these via goals or mavenOpts, as they are pointless.
     */
    public static final String[] SUPERFLUOUS_OPTIONS = {
        "-B",
        "--batch-mode",
        "-batch-mode",
        "-V",
        "--show-version",
        "-show-version"
        // Maven will also warn about deprecated options, so no need to add checks for them here
    };

    /**
     * Complain if any superfluous options were configured.
     */
    boolean detectSuperfluousOptions(final ArgumentListBuilder args) {
        assert args != null;
        boolean detected = false;
        for (String arg : args.toCommandArray()) {
            for (String opt : SUPERFLUOUS_OPTIONS) {
                if (arg.equals(opt)) {
                    log.warn("Detected superfluous option: {}", arg);
                    detected = true;
                }
            }
        }
        return detected;
    }

    /**
     * Banned options, users should be warned when configuring these via goals or mavenOpts, as this will prevent the integration from functioning
     * correctly.
     */
    public static final String[] BANNED_OPTIONS = {
        "--help",
        "-help",
        "-h",
        "--version",
        "-version",
        "-v",
        "-ep",
        "--encrypt-password",
        "-encrypt-password",
        "-emp",
        "--encrypt-master-password",
        "-encrypt-master-password"
    };

    /**
     * Complain if any banned options were configured.
     */
    boolean detectBannedOptions(final ArgumentListBuilder args) {
        assert args != null;
        boolean detected = false;
        for (String arg : args.toCommandArray()) {
            for (String opt : BANNED_OPTIONS) {
                if (arg.equals(opt)) {
                    log.error("Detected banned option: {}", arg);
                    detected = true;
                }
            }
        }
        return detected;
    }

    /**
     * Banned properties (prefixes in arg form), users should be warned when configuring these via goals or mavenOpts, as they may prevent the
     * integration from functioning correctly.
     */
    public static final String[] BANNED_PROPERTIES = {
        "-Dmaven.ext.class.path=",
        "-Dhudson.eventspy."
    };

    /**
     * Complain if any banned properties were configured.
     */
    public boolean detectBannedProperties(final ArgumentListBuilder args) {
        assert args != null;
        boolean detected = false;
        for (String arg : args.toCommandArray()) {
            for (String prop : BANNED_PROPERTIES) {
                if (arg.startsWith(prop)) {
                    log.warn("Detected banned property: {}", arg);
                    detected = true;
                }
            }
        }
        return detected;
    }
}
