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

import org.eclipse.hudson.maven.plugin.builder.MavenBuilder;
import org.eclipse.hudson.maven.plugin.install.BundledMavenInstallation;
import org.eclipse.hudson.maven.plugin.install.MavenInstallation;
import org.eclipse.hudson.utils.tasks.OpenServerSocket;
import org.eclipse.hudson.utils.tasks.PerformOperation;
import org.eclipse.hudson.maven.model.PropertiesDTO;
import org.eclipse.hudson.maven.model.state.BuildResultDTO;
import org.eclipse.hudson.maven.model.state.BuildStateDTO;
import org.eclipse.hudson.maven.model.state.BuildSummaryDTO;
import org.eclipse.hudson.maven.model.state.ExecutionActivityDTO;
import org.eclipse.hudson.maven.model.state.ExecutionActivityTypeDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.remoting.Channel;
import hudson.slaves.Channels;
import hudson.util.VariableResolver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;


import static org.eclipse.hudson.maven.plugin.builder.internal.MavenConstants.JAVA_HOME;
import static org.eclipse.hudson.maven.plugin.builder.internal.MavenConstants.MAVEN_REPO;
import static org.eclipse.hudson.maven.plugin.builder.internal.MavenConstants.MAVEN_TMP;
import static org.model.hudson.maven.eventspy.common.Constants.INVOKE_RECORD_FILE;

/**
 * {@link MavenBuilder} perform build operation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class PerformBuild
    extends PerformOperation<MavenBuilder>
{
    private static final List<BuildResultDTO> INCOMPLETE_STATES =
        Collections.unmodifiableList(Arrays.asList(null, BuildResultDTO.SCHEDULED, BuildResultDTO.BUILDING, BuildResultDTO.UNKNOWN));

    private final BuildStateDTO state;

    public PerformBuild(final MavenBuilder owner, BuildStateDTO state, final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) {
        super(owner, build, launcher, listener);
        this.state = state;
    }

    @Override
    protected boolean doExecute() throws Exception {
        final EnvVars env = build.getEnvironment(listener);
        final VariableResolver<String> vr = build.getBuildVariableResolver();

        MavenProcessBuilder builder = new MavenProcessBuilder(muxlog)
        {
            @Override
            protected String resolve(String value) {
                assert value != null;
                value = Util.replaceMacro(value, vr);
                value = env.expand(value);
                value = value.replaceAll("[\t\r\n]+", " ");
                return value;
            }
        };

        // Resolve and validate the installation
        MavenInstallation installation = owner.getMavenInstallation();
        if (installation == null) {
            muxlog.info("Using bundled Maven 3 installation");
            installation = new BundledMavenInstallation();
        }
        else {
            muxlog.info("Using Maven 3 installation: {}",
                    installation.getName());
            installation = installation.forNode(owner.getNodes().getCurrentNode(), listener);
            installation = installation.forEnvironment(env);
        }

        // Complain if JAVA_HOME is not set... build may or may not work
        if (!env.containsKey(JAVA_HOME)) {
            muxlog.warn("{} is not configured; results may be unpredictable", JAVA_HOME);
        }

        MavenInstallationValidator validator = new MavenInstallationValidator(installation, build, env, launcher, listener);
        validator.validate();

        // Configure mvn to use a workspace local tmp dir, make sure the dir exists
        FilePath tmpDir = build.getWorkspace().child(MAVEN_TMP);
        tmpDir.mkdirs();

        // Build the arguments for the Maven process
        builder
            .withWindows(!launcher.isUnix())
            .withConfiguration(owner.getConfig())
            .withMavenHome(validator.getHome())
            .withMavenExecutable(validator.getExecutable())
            .withExtClasspath(validator.getExtClasspath())
            .withBuildVariables(build.getBuildVariables())
            .withEnv(env)
            .withWorkingDirectory(build.getWorkspace())
            .withRepository(build.getWorkspace().child(MAVEN_REPO))
            .withTmpDir(tmpDir)
            .withStandardOutput(listener.getLogger());

        // Open up a sever-socket on the remote node
        OpenServerSocket.Acceptor acceptor = launcher.getChannel().call(new OpenServerSocket());
        int port = acceptor.getPort();
        builder.withPort(port);

        // Launch the Maven process on the remote node
        Launcher.ProcStarter starter = builder.build(launcher.launch());
        Proc process = starter.start();

        muxlog.debug("Waiting for connection on port: {}", port);

        // Accept a single connection from the client process
        OpenServerSocket.Connection connection;
        try {
            connection = acceptor.accept(true);
        }
        catch (SocketTimeoutException e) {
            log.debug("Failed to accept connection", e);

            // If the process is dead, complain
            if (!process.isAlive()) {
                String message = "Process did not initiate connection and appears to have died; exit code: " + process.join();
                muxlog.error(message);
                throw new AbortException(message);
            }

            // else kill it and complain
            muxlog.error("Process did not initiate connection and is still alive; killing it");
            process.kill();
            throw new AbortException("Process failed to connect; exit code: " + process.join());
        }

        // Setup the remoting server's channel
        Channel channel = Channels.forProcess( // FIXME: Expose from a service
                "EventSpy:" + starter.cmds(),
                Computer.threadPoolForRemoting, // FIXME: Expose from a service
                new BufferedInputStream(connection.getInput()),
                new BufferedOutputStream(connection.getOutput()),
                null, // listener.getLogger(), <-- This writes out the icky looking <=== *** header only
                process
        );

        // At this point we should be able to run commands on the new process
        muxlog.debug("Connected to remote");

        // Start processing
        CallbackImpl callback = new CallbackImpl(owner, state, build);
        StartProcessing command = new StartProcessing(callback);

        // Optionally configure callback invocations to be recorded if configured in the builders properties
        PropertiesDTO props = owner.getConfig().getProperties();
        if (props != null && props.contains(INVOKE_RECORD_FILE)) {
            FilePath path = build.getWorkspace().child(props.get(INVOKE_RECORD_FILE));
            muxlog.info("Recording invocations to: {}", path);
            command.recordInvocationsTo(path);
        }

        channel.call(command);

        // Clean up
        muxlog.debug("Closing connection to remote");

        // Client closes channel normally, so we can just close here; join will block
        channel.close();
        connection.close();
        acceptor.close();

        // Wait for the process to finish
        muxlog.debug("Waiting for process to finish");

        int result = process.join();

        muxlog.debug("Result: {}", result);

        return result == 0;
    }
    
    @Override
    protected void onFailure(final Throwable cause) {
        super.onFailure(cause);
        
        // TODO: consider failure = abort for all cases or just specific exceptions like InterruptedException?
        
        state.getExecutionActivities().add(
            new ExecutionActivityDTO().withType(ExecutionActivityTypeDTO.ABORTED).withTimestamp(new Date()));
        
        for (MavenProjectDTO project : state.getParticipatingProjects()) {
            BuildSummaryDTO buildSummary = project.getBuildSummary();

            // Projects missing build summary; this would occur if aborted at the very beginning of Maven project analysis.
            if (null == buildSummary) {
                project.setBuildSummary(new BuildSummaryDTO().withResult(BuildResultDTO.ABORTED));
            }
            // Incomplete build states.
            else if (INCOMPLETE_STATES.contains(buildSummary.getResult())) {
                buildSummary.withResult(BuildResultDTO.ABORTED);
            }
        }
    }
}
