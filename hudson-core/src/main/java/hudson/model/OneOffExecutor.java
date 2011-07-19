/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package hudson.model;

import hudson.model.Queue.FlyweightTask;
import hudson.model.queue.WorkUnit;

/**
 * {@link Executor} that's temporarily added to carry out tasks that doesn't consume
 * regular executors, like a matrix project parent build.
 *
 * @author Kohsuke Kawaguchi
 * @see FlyweightTask
 */
public class OneOffExecutor extends Executor {
    private WorkUnit work;

    public OneOffExecutor(Computer owner, WorkUnit work) {
        super(owner,-1);
        this.work = work;
    }

    @Override
    protected boolean shouldRun() {
        // TODO: consulting super.shouldRun() here means we'll lose the work if it gets scheduled
        // when super.shouldRun() returns false.
        return super.shouldRun() && work !=null;
    }

    @Override
    protected WorkUnit grabJob() throws InterruptedException {
        WorkUnit r = work;
        work = null;
        return r;
    }

    @Override
    public void run() {
        try {
            super.run();
        } finally {
            owner.remove(this);
        }
    }
}
