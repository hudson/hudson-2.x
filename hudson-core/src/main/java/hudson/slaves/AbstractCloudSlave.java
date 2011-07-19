/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.slaves;

import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.model.Slave;
import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Partial implementation of {@link Slave} to be used by {@link AbstractCloudImpl}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.382
 */
public abstract class AbstractCloudSlave extends Slave {
    public AbstractCloudSlave(String name, String nodeDescription, String remoteFS, String numExecutors, Mode mode, String labelString, ComputerLauncher launcher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties) throws FormException, IOException {
        super(name, nodeDescription, remoteFS, numExecutors, mode, labelString, launcher, retentionStrategy, nodeProperties);
    }

    public AbstractCloudSlave(String name, String nodeDescription, String remoteFS, int numExecutors, Mode mode, String labelString, ComputerLauncher launcher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties) throws FormException, IOException {
        super(name, nodeDescription, remoteFS, numExecutors, mode, labelString, launcher, retentionStrategy, nodeProperties);
    }

    @Override
    public abstract AbstractCloudComputer createComputer();

    /**
     * Releases and removes this slave.
     */
    public void terminate() throws InterruptedException, IOException {
        try {
            // TODO: send the output to somewhere real
            _terminate(new StreamTaskListener(System.out, Charset.defaultCharset()));
        } finally {
            try {
                Hudson.getInstance().removeNode(this);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to remove "+name,e);
            }
        }
    }

    /**
     * Performs the removal of the underlying resource from the cloud.
     */
    protected abstract void _terminate(TaskListener listener) throws IOException, InterruptedException;

    private static final Logger LOGGER = Logger.getLogger(AbstractCloudSlave.class.getName());
}
