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

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link VirtualChannel} that performs computation on the local JVM.
 * 
 * @author Kohsuke Kawaguchi
 */
public class LocalChannel implements VirtualChannel {
    private final ExecutorService executor;

    public LocalChannel(ExecutorService executor) {
        this.executor = executor;
    }

    public <V, T extends Throwable> V call(Callable<V,T> callable) throws T {
        return callable.call();
    }

    public <V, T extends Throwable> Future<V> callAsync(final Callable<V,T> callable) throws IOException {
        final java.util.concurrent.Future<V> f = executor.submit(new java.util.concurrent.Callable<V>() {
            public V call() throws Exception {
                try {
                    return callable.call();
                } catch (Exception t) {
                    throw t;
                } catch (Error t) {
                    throw t;
                } catch (Throwable t) {
                    throw new ExecutionException(t);
                }
            }
        });

        return new Future<V>() {
            public boolean cancel(boolean mayInterruptIfRunning) {
                return f.cancel(mayInterruptIfRunning);
            }

            public boolean isCancelled() {
                return f.isCancelled();
            }

            public boolean isDone() {
                return f.isDone();
            }

            public V get() throws InterruptedException, ExecutionException {
                return f.get();
            }

            public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return f.get(timeout,unit);
            }
        };
    }

    public void close() {
        // noop
    }

    public void join() throws InterruptedException {
        // noop
    }

    public void join(long timeout) throws InterruptedException {
        // noop
    }

    public <T> T export(Class<T> intf, T instance) {
        return instance;
    }
}
