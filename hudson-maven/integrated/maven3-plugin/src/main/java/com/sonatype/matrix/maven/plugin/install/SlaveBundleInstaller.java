/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.install;

import com.sonatype.matrix.events.EventConsumer;
import com.sonatype.matrix.events.ready.ReadyEvent;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.slaves.ComputerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.util.EventObject;

/**
 * Handles installation of the slave bundle.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Named
@Singleton
public class SlaveBundleInstaller
    extends ComputerListener
    implements EventConsumer
{
    private static final Logger log = LoggerFactory.getLogger(SlaveBundleInstaller.class);

    public static final String BASE_PATH = "maven/slavebundle";

    public static final String BUNDLE_ARCHIVE = "matrix-maven-slavebundle.zip";

    private boolean install(final FilePath root, TaskListener listener) throws IOException, InterruptedException {
        assert root != null;
        if (listener == null) {
            listener = TaskListener.NULL;
        }

        String resource = BUNDLE_ARCHIVE;
        URL url = SlaveBundleInstaller.class.getResource(resource);
        if (url == null) {
            throw new RuntimeException("Unable to install Maven slave bundle; missing resource: " + resource);
        }

        FilePath dir = new FilePath(root, BASE_PATH);
        dir.mkdirs();
        log.debug("Maven slave bundle installation directory: {}", dir);

        return dir.installIfNecessaryFrom(url, listener, "Installing: " + resource);
    }

    /**
     * Handles installation onto the master node.
     */
    public void consume(final EventObject event) throws Exception {
        if (event instanceof ReadyEvent) {
            ReadyEvent target = (ReadyEvent)event;
            FilePath root = target.getHudson().getRootPath();

            if (install(root, null)) {
                log.info("Maven slave bundle installed");
            }
        }
    }

    /**
     * Handles installation onto slave nodes.
     */
    @Override
    public void preOnline(final Computer c, final Channel channel, final FilePath root, final TaskListener listener)
        throws IOException, InterruptedException
    {
        if (install(root, listener)) {
            listener.getLogger().println("done");
            log.info("Maven slave bundle installed on computer: {}", c.getName());
        }
    }

    /**
     * Get a reference to the installed bundle root.
     */
    public static FilePath getInstallRoot() throws IOException, InterruptedException {
        return getInstallRoot(Executor.currentExecutor().getOwner().getNode());
    }

    /**
     * Get a reference to the installed bundle root.
     */
    public static FilePath getInstallRoot(final Node node) throws IOException, InterruptedException {
        return node.getRootPath().child(BASE_PATH);
    }
}