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

/**
 * Estimated future load to Hudson.
 *
 * @author Kohsuke Kawaguchi
 * @see LoadPredictor
 */
public final class FutureLoad {
    /**
     * When is this load expected to start?
     */
    public final long startTime;
    /**
     * How many executors is this going to consume?
     */
    public final int numExecutors;
    /**
     * How long is task expected to continue, in milliseconds?
     */
    public final long duration;

    public FutureLoad(long startTime, long duration, int numExecutors) {
        this.startTime = startTime;
        this.numExecutors = numExecutors;
        this.duration = duration;
    }

    public String toString() {
        return "startTime="+startTime+",#executors="+numExecutors+",duration="+duration;
    }
}
