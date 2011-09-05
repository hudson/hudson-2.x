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

import hudson.model.Executor;
import hudson.model.Queue;
import hudson.model.Queue.Executable;
import hudson.model.Queue.Task;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Represents a unit of hand-over to {@link Executor} from {@link Queue}.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.377
 */
@ExportedBean
public final class WorkUnit {
    /**
     * Task to be executed.
     */
    //TODO: review and check whether we can do it private
    public final SubTask work;

    /**
     * Shared context among {@link WorkUnit}s.
     */
    //TODO: review and check whether we can do it private
    public final WorkUnitContext context;

    private volatile Executor executor;

    WorkUnit(WorkUnitContext context, SubTask work) {
        this.context = context;
        this.work = work;
    }

    public SubTask getWork() {
        return work;
    }

    public WorkUnitContext getContext() {
        return context;
    }

    /**
     * {@link Executor} running this work unit.
     * <p>
     * {@link Executor#getCurrentWorkUnit()} and {@link WorkUnit#getExecutor()}
     * form a bi-directional reachability between them.
     */
    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor e) {
        executor = e;
    }

    /**
     * If the execution has already started, return the current executable.
     */
    public Executable getExecutable() {
        return executor!=null ? executor.getCurrentExecutable() : null;
    }

    /**
     * Is this work unit the "main work", which is the primary {@link SubTask}
     * represented by {@link Task} itself.
     */
    public boolean isMainWork() {
        return context.task==work;
    }
}
