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

package hudson.model.queue;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Executor;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Queue.Executable;
import hudson.model.Queue.Task;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestExtension;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Kohsuke Kawaguchi
 */
public class WideExecutionTest extends HudsonTestCase {
    @TestExtension
    public static class Contributer extends SubTaskContributor {
        public Collection<? extends SubTask> forProject(final AbstractProject<?, ?> p) {
            return Collections.singleton(new AbstractSubTask() {
                private final AbstractSubTask outer = this;
                public Executable createExecutable() throws IOException {
                    return new Executable() {
                        public SubTask getParent() {
                            return outer;
                        }

                        public void run() {
                            WorkUnitContext wuc = Executor.currentExecutor().getCurrentWorkUnit().context;
                            AbstractBuild b = (AbstractBuild)wuc.getPrimaryWorkUnit().getExecutable();
                            try {
                                b.setDescription("I was here");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        public long getEstimatedDuration() {
                            return 0;
                        }
                    };
                }

                public Task getOwnerTask() {
                    return p;
                }

                public String getDisplayName() {
                    return "Company of "+p.getDisplayName();
                }
            });
        }
    }

    public void testRun() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        FreeStyleBuild b = assertBuildStatusSuccess(p.scheduleBuild2(0));
        assertTrue(b.getDescription().equals("I was here"));
    }
}
