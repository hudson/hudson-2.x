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
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.slaves;

import hudson.model.Descriptor;
import hudson.model.TaskListener;

import java.io.IOException;

/**
 * {@link ComputerLauncher} filter that can be used as a base class for decorators.
 *
 * <p>
 * Using this class also protects you from method additions in {@link ComputerLauncher},
 * since these two classes are updated in sync.
 *
 * @author Kohsuke Kawaguchi
 * @see SlaveComputer#grabLauncher(Node)
 */
public abstract class ComputerLauncherFilter extends ComputerLauncher {
    protected volatile ComputerLauncher core;

    public ComputerLauncherFilter(ComputerLauncher core) {
        this.core = core;
    }

    /**
     * Returns the delegation target.
     */
    public ComputerLauncher getCore() {
        return core;
    }

    @Override
    public boolean isLaunchSupported() {
        return core.isLaunchSupported();
    }

    @Override
    public void launch(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException {
        core.launch(computer, listener);
    }

    @Override
    public void afterDisconnect(SlaveComputer computer, TaskListener listener) {
        core.afterDisconnect(computer, listener);
    }

    @Override
    public void beforeDisconnect(SlaveComputer computer, TaskListener listener) {
        core.beforeDisconnect(computer, listener);
    }

    @Override
    public Descriptor<ComputerLauncher> getDescriptor() {
        throw new UnsupportedOperationException();
    }
}
