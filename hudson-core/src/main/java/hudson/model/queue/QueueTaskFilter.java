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

import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.Queue.Executable;
import hudson.model.Queue.Task;
import hudson.model.ResourceList;

import java.io.IOException;
import java.util.Collection;

/**
 * Base class for defining filter {@link Queue.Task}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.360
 */
public abstract class QueueTaskFilter implements Queue.Task {
    private final Queue.Task base;

    protected QueueTaskFilter(Task base) {
        this.base = base;
    }

    public Label getAssignedLabel() {
        return base.getAssignedLabel();
    }

    public Node getLastBuiltOn() {
        return base.getLastBuiltOn();
    }

    public boolean isBuildBlocked() {
        return base.isBuildBlocked();
    }

    public String getWhyBlocked() {
        return base.getWhyBlocked();
    }

    public CauseOfBlockage getCauseOfBlockage() {
        return base.getCauseOfBlockage();
    }

    public String getName() {
        return base.getName();
    }

    public String getFullDisplayName() {
        return base.getFullDisplayName();
    }

    public long getEstimatedDuration() {
        return base.getEstimatedDuration();
    }

    public Executable createExecutable() throws IOException {
        return base.createExecutable();
    }

    public void checkAbortPermission() {
        base.checkAbortPermission();
    }

    public boolean hasAbortPermission() {
        return base.hasAbortPermission();
    }

    public String getUrl() {
        return base.getUrl();
    }

    public boolean isConcurrentBuild() {
        return base.isConcurrentBuild();
    }

    public String getDisplayName() {
        return base.getDisplayName();
    }

    public ResourceList getResourceList() {
        return base.getResourceList();
    }

    public Collection<? extends SubTask> getSubTasks() {
        return base.getSubTasks();
    }

    public final Task getOwnerTask() {
        return this;
    }

    public Object getSameNodeConstraint() {
        return base.getSameNodeConstraint();
    }
}
