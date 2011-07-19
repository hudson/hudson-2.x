/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package org.apache.maven.lifecycle;

import org.apache.maven.BuildFailureException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ReactorManager;
import org.apache.maven.monitor.event.EventDispatcher;

import java.io.IOException;

/**
 * Event notification for the start/end of the maven execution.
 *
 * <p>
 * The exact semantics in Maven is undocumented (as usual!), but apparently
 * this is invoked at the beginning of the build and the end, surrounding
 * the complete mojo executions.
 *
 * @author Kohsuke Kawaguchi
 */
public interface LifecycleExecutorListener {
    void preBuild(MavenSession session, ReactorManager rm, EventDispatcher dispatcher) throws BuildFailureException, LifecycleExecutionException, InterruptedException, IOException;
    void postBuild(MavenSession session, ReactorManager rm, EventDispatcher dispatcher) throws BuildFailureException, LifecycleExecutionException, InterruptedException, IOException;

    /**
     * This event is avaialble from {@link LifecycleExecutorListener}
     * and offers accurate "leaving module" event.
     */
    void endModule() throws InterruptedException, IOException;
}
