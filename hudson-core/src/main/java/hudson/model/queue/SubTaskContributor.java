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

import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractProject;
import hudson.model.Hudson;

import java.util.Collection;
import java.util.Collections;

/**
 * Externally contributes {@link SubTask}s to {@link AbstractProject#getSubTasks()}.
 *
 * <p>
 * Put @{@link Extension} on your implementation classes to register them.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.377
 */
public abstract class SubTaskContributor implements ExtensionPoint {
    public Collection<? extends SubTask> forProject(AbstractProject<?,?> p) {
        return Collections.emptyList();
    }

    /**
     * All registered {@link MemberExecutionUnitContributor} instances.
     */
    public static ExtensionList<SubTaskContributor> all() {
        return Hudson.getInstance().getExtensionList(SubTaskContributor.class);
    }
}
