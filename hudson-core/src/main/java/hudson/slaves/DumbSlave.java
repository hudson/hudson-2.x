/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi, Seiji Sogabe
 *     
 *
 *******************************************************************************/ 

package hudson.slaves;

import hudson.model.Slave;
import hudson.model.Descriptor.FormException;
import hudson.Extension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Default {@link Slave} implementation for computers that do not belong to a higher level structure,
 * like grid or cloud.
 *
 * @author Kohsuke Kawaguchi
 */
public final class DumbSlave extends Slave {
    /**
     * @deprecated as of 1.286.
     *      Use {@link #DumbSlave(String, String, String, String, Mode, String, ComputerLauncher, RetentionStrategy, List)}
     */
    public DumbSlave(String name, String nodeDescription, String remoteFS, String numExecutors, Mode mode, String labelString, ComputerLauncher launcher, RetentionStrategy retentionStrategy) throws FormException, IOException {
        this(name, nodeDescription, remoteFS, numExecutors, mode, labelString, launcher, retentionStrategy, new ArrayList());
    }
    
    @DataBoundConstructor
    public DumbSlave(String name, String nodeDescription, String remoteFS, String numExecutors, Mode mode, String labelString, ComputerLauncher launcher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties) throws IOException, FormException {
    	super(name, nodeDescription, remoteFS, numExecutors, mode, labelString, launcher, retentionStrategy, nodeProperties);
    }

    @Extension
    public static final class DescriptorImpl extends SlaveDescriptor {
        public String getDisplayName() {
            return Messages.DumbSlave_displayName();
        }
    }
}
