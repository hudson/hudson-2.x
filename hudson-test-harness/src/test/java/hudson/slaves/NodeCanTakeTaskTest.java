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

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Messages;
import hudson.model.Node;
import hudson.model.Node.Mode;
import hudson.model.Project;
import hudson.model.Result;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.Task;
import hudson.model.Slave;
import hudson.model.queue.CauseOfBlockage;

import junit.framework.Assert;

import org.jvnet.hudson.test.HudsonTestCase;

public class NodeCanTakeTaskTest extends HudsonTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Set master executor count to zero to force all jobs to slaves
        hudson.setNumExecutors(0);
    }

    public void testTakeBlockedByProperty() throws Exception {
        Slave slave = createSlave();
        FreeStyleProject project = createFreeStyleProject();

        // First, attempt to run our project before adding the property
        Future<FreeStyleBuild> build = project.scheduleBuild2(0);
        assertBuildStatus(Result.SUCCESS, build.get(10, TimeUnit.SECONDS));

        // Add the build-blocker property and try again
        slave.getNodeProperties().add(new RejectAllTasksProperty());

        build = project.scheduleBuild2(0);
        try {
            build.get(10, TimeUnit.SECONDS);
            fail("Expected timeout exception");
        } catch (TimeoutException e) {
            List<BuildableItem> buildables = hudson.getQueue().getBuildableItems();
            Assert.assertNotNull(buildables);
            Assert.assertEquals(1, buildables.size());

            BuildableItem item = buildables.get(0);
            Assert.assertEquals(project, item.task);
            Assert.assertNotNull(item.getCauseOfBlockage());
            Assert.assertEquals(Messages.Queue_WaitingForNextAvailableExecutor(), item.getCauseOfBlockage().getShortDescription());
        }
    }

    private static class RejectAllTasksProperty extends NodeProperty<Node> {
        @Override
        public CauseOfBlockage canTake(Task task) {
            return CauseOfBlockage.fromMessage(null);
        }
    }
}
