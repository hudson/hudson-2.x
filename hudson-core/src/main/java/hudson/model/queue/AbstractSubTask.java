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
import hudson.model.ResourceList;

/**
 * Partial default implementation of {@link SubTask} to avoid
 * {@link AbstractMethodError} with future additions to {@link SubTask}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractSubTask implements SubTask {
    public Label getAssignedLabel() {
        return null;
    }

    public Node getLastBuiltOn() {
        return null;
    }

    public long getEstimatedDuration() {
        return -1;
    }

    public Object getSameNodeConstraint() {
        return null;
    }

    public ResourceList getResourceList() {
        return new ResourceList();
    }
}
