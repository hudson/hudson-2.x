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

package hudson.remoting;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link Future} that converts the return type.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class FutureAdapter<X,Y> implements Future<X> {
    protected final Future<Y> core;

    protected FutureAdapter(Future<Y> core) {
        this.core = core;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return core.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled() {
        return core.isCancelled();
    }

    public boolean isDone() {
        return core.isDone();
    }

    public X get() throws InterruptedException, ExecutionException {
        return adapt(core.get());
    }

    public X get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return adapt(core.get(timeout, unit));
    }

    protected abstract X adapt(Y y) throws ExecutionException;
}
