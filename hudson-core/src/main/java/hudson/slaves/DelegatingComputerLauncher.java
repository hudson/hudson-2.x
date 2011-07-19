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

import hudson.Functions;
import hudson.model.Descriptor;
import hudson.model.TaskListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenient base implementation of {@link ComputerLauncher} that allows
 * subtypes to perform some initialization (typically something cloud/v12n related
 * to power up the machine), then to delegate to another {@link ComputerLauncher}
 * to connect.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.382
 */
public abstract class DelegatingComputerLauncher extends ComputerLauncher {
    protected ComputerLauncher launcher;

    protected DelegatingComputerLauncher(ComputerLauncher launcher) {
        this.launcher = launcher;
    }

    public ComputerLauncher getLauncher() {
        return launcher;
    }

    @Override
    public void launch(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException {
        getLauncher().launch(computer, listener);
    }

    @Override
    public void afterDisconnect(SlaveComputer computer, TaskListener listener) {
        getLauncher().afterDisconnect(computer, listener);
    }

    @Override
    public void beforeDisconnect(SlaveComputer computer, TaskListener listener) {
        getLauncher().beforeDisconnect(computer, listener);
    }

    public static abstract class DescriptorImpl extends Descriptor<ComputerLauncher> {
        /**
         * Returns the applicable nested computer launcher types.
         * The default implementation avoids all delegating descriptors, as that creates infinite recursion.
         */
        public List<Descriptor<ComputerLauncher>> getApplicableDescriptors() {
            List<Descriptor<ComputerLauncher>> r = new ArrayList<Descriptor<ComputerLauncher>>();
            for (Descriptor<ComputerLauncher> d : Functions.getComputerLauncherDescriptors()) {
                if (DelegatingComputerLauncher.class.isInstance(d))  continue;
                r.add(d);
            }
            return r;
        }
    }

}
