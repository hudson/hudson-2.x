/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.tasks;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.CheckPoint;
import hudson.Launcher;

import java.io.IOException;

/**
 * Used by {@link BuildStep#getRequiredMonitorService()}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.319
 */
public enum BuildStepMonitor {
    NONE {
        public boolean perform(BuildStep bs, AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
            return bs.perform(build,launcher,listener);
        }
    },
    STEP {
        public boolean perform(BuildStep bs, AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
            CheckPoint cp = new CheckPoint(bs.getClass().getName(),bs.getClass());
            cp.block();
            try {
                return bs.perform(build,launcher,listener);
            } finally {
                cp.report();
            }
        }
    },
    BUILD {
        public boolean perform(BuildStep bs, AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
            CheckPoint.COMPLETED.block();
            return bs.perform(build,launcher,listener);
        }
    };

    /**
     * Calls {@link BuildStep#perform(AbstractBuild, Launcher, BuildListener)} with the proper synchronization.
     */
    public abstract boolean perform(BuildStep bs, AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException;
}
