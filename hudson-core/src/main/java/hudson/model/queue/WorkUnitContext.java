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

import hudson.model.Action;
import hudson.model.Executor;
import hudson.model.Queue;
import hudson.model.Queue.BuildableItem;
import hudson.model.Queue.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the information shared between {@link WorkUnit}s created from the same {@link Task}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class WorkUnitContext {

    //TODO: review and check whether we can do it private
    public final BuildableItem item;

    //TODO: review and check whether we can do it private
    public final Task task;

    /**
     * Once the execution is complete, update this future object with the outcome.
     */
    //TODO: review and check whether we can do it private
    public final FutureImpl future;

    /**
     * Associated parameters to the build.
     */
    //TODO: review and check whether we can do it private
    public final List<Action> actions;

    private final Latch startLatch, endLatch;

    private List<WorkUnit> workUnits = new ArrayList<WorkUnit>();

    /**
     * If the execution is aborted, set to non-null that indicates where it was aborted.
     */
    private volatile Throwable aborted;

    public WorkUnitContext(BuildableItem item) {
        this.item = item;
        this.task = item.task;
        this.future = (FutureImpl)item.getFuture();
        this.actions = item.getActions();
        
        // +1 for the main task
        int workUnitSize = Tasks.getSubTasksOf(task).size();
        startLatch = new Latch(workUnitSize) {
            @Override
            protected void onCriteriaMet() {
                // on behalf of the member Executors,
                // the one that executes the main thing will send notifications
                Executor e = Executor.currentExecutor();
                if (e.getCurrentWorkUnit().isMainWork()) {
                    e.getOwner().taskAccepted(e,task);
                }
            }
        };

        endLatch = new Latch(workUnitSize);
    }

    public BuildableItem getItem() {
        return item;
    }

    public Task getTask() {
        return task;
    }

    public FutureImpl getFuture() {
        return future;
    }

    public List<Action> getActions() {
        return actions;
    }

    /**
     * Called by the executor that executes a member {@link SubTask} that belongs to this task
     * to create its {@link WorkUnit}.
     */
    public WorkUnit createWorkUnit(SubTask execUnit) {
        future.addExecutor(Executor.currentExecutor());
        WorkUnit wu = new WorkUnit(this, execUnit);
        workUnits.add(wu);
        return wu;
    }

    public List<WorkUnit> getWorkUnits() {
        return Collections.unmodifiableList(workUnits);
    }

    public WorkUnit getPrimaryWorkUnit() {
        return workUnits.get(0);
    }

    /**
     * All the {@link Executor}s that jointly execute a {@link Task} call this method to synchronize on the start.
     */
    public void synchronizeStart() throws InterruptedException {
        startLatch.synchronize();
    }

    /**
     * All the {@link Executor}s that jointly execute a {@link Task} call this method to synchronize on the end of the task.
     *
     * @throws InterruptedException
     *      If any of the member thread is interrupted while waiting for other threads to join, all
     *      the member threads will report {@link InterruptedException}.
     */
    public void synchronizeEnd(Queue.Executable executable, Throwable problems, long duration) throws InterruptedException {
        endLatch.synchronize();

        // the main thread will send a notification
        Executor e = Executor.currentExecutor();
        WorkUnit wu = e.getCurrentWorkUnit();
        if (wu.isMainWork()) {
            if (problems == null) {
                future.set(executable);
                e.getOwner().taskCompleted(e, task, duration);
            } else {
                future.set(problems);
                e.getOwner().taskCompletedWithProblems(e, task, duration, problems);
            }
        }
    }

    /**
     * When one of the work unit is aborted, call this method to abort all the other work units.
     */
    public synchronized void abort(Throwable cause) {
        if (cause==null)        throw new IllegalArgumentException();
        if (aborted!=null)      return; // already aborted    
        aborted = cause;
        startLatch.abort(cause);
        endLatch.abort(cause);

        Thread c = Thread.currentThread();
        for (WorkUnit wu : workUnits) {
            Executor e = wu.getExecutor();
            if (e!=null && e!=c)
                e.interrupt();
        }
    }
}
