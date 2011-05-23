/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30;

import com.sonatype.matrix.maven.eventspy.common.Callback;
import com.sonatype.matrix.maven.eventspy.common.CallbackManager;
import com.sonatype.matrix.maven.eventspy.common.RemotingClient;
import com.sonatype.matrix.maven.model.PropertiesDTOHelper;
import com.sonatype.matrix.maven.model.state.RuntimeEnvironmentDTO;
import org.apache.maven.BuildAbort;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sonatype.matrix.maven.eventspy.common.Constants.CALLBACK_WAIT_TIMEOUT;
import static com.sonatype.matrix.maven.eventspy.common.Constants.CALLBACK_WAIT_TIMEOUT_UNIT;
import static com.sonatype.matrix.maven.eventspy.common.Constants.PORT_PROPERTY;

/**
 * Matrix {@link org.apache.maven.eventspy.EventSpy} for Maven 3.0 integration.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Named
public class RemotingEventSpy
    extends EventSpySupport
{
    private final EventSpyProcessor processor;

    private RemotingClient client;

    private Callback callback;

    @Inject
    public RemotingEventSpy(final List<EventSpyHandler> handlers) {
        checkNotNull( handlers );
        this.processor = new EventSpyProcessor(handlers, null);
    }

    /**
     * Returns the port number to be used when establishing communication.
     *
     * @throws Error    If the port number has not been configured.
     */
    private int getPort() {
        String tmp = getProperty(PORT_PROPERTY);
        if (tmp == null) {
            throw new Error("Missing port number");
        }
        return Integer.parseInt(tmp);
    }

    /**
     * Opens remoting channel.
     *
     * @throws Error    Failed to open channel.
     */
    private void openChannel() throws Exception {
        int port = getPort();
        client = new RemotingClient(port);
        client.open();
    }

    /**
     * Closes the remoting channel.
     */
    private void closeChannel() throws Exception {
        client.join();
        client.close();
    }

    /**
     * Invoked by Maven startup process.
     *
     * Initiates the remoting connection back to controlling MavenBuilder component.
     * Waits for callback reference, blocking until received (or times out).
     * Initializes the event processor with context.
     *
     * @throws Error    Initialization failed.
     */
    @Override
    public void init(final Context context) throws Exception {
        checkNotNull( context );

        log.debug("Initializing");

        super.init(context);

        openChannel();

        // Wait for the callback
        callback = CallbackManager.get(CALLBACK_WAIT_TIMEOUT, CALLBACK_WAIT_TIMEOUT_UNIT);

        // For now we are going to use the standard logging to STDOUT that Maven does
        // DefaultPlexusContainer container = getContainer();
        // container.setLoggerManager(new LoggerManagerImpl(this, container.getLoggerManager()));

        // Initialize the processor
        processor.init(new EventSpyHandler.HandlerContext(callback));

        // Expose Maven's environment (props, version, envvars, etc)
        RuntimeEnvironmentDTO env = new RuntimeEnvironmentDTO()
            .withVersionProperties(PropertiesDTOHelper.convert(getVersionProperties()))
            .withUserProperties(PropertiesDTOHelper.convert(getUserProperties()))
            .withSystemProperties(PropertiesDTOHelper.convert(getSystemProperties()))
            .withSystemEnvironment(PropertiesDTOHelper.convert(System.getenv()))
            .withWorkingDirectory(getWorkingDirectory().getCanonicalPath());
        callback.setRuntimeEnvironment(env);

        // Should be all ready now to handle event stream
        log.debug("Ready");
    }

    /**
     * Performs event processing.
     *
     * Handles throwing {@link BuildAbort}.
     */
    @Override
    public void onEvent(final Object event) throws Exception {
        checkNotNull( event );

        ensureOpened();

        // FIXME: This is way too chatty
//        // First check if we are aborted
//        if (callback.isAborted()) {
//            // TODO: May want to see if this can be pushed, instead of pulled
//            log.debug("Aborting build");
//            throw new BuildAbort("Aborted");
//        }

        // Delegate handling to processor
        processor.process(event);
    }

    @Override
    public void close() throws Exception {
        log.debug("Closing");

        // Signal that we are finished
        try {
            callback.close();

            // Shutdown the connection
            closeChannel();
        }
        finally {
            super.close();
        }

        log.debug("Closed");
    }
}