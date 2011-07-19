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

/**
 * Virtualized {@link Channel} that allows different implementations.
 * 
 * @author Kohsuke Kawaguchi
 */
public interface VirtualChannel {
    /**
     * Makes a remote procedure call.
     *
     * <p>
     * Sends {@link Callable} to the remote system, executes it, and returns its result.
     *
     * @throws InterruptedException
     *      If the current thread is interrupted while waiting for the completion.
     * @throws IOException
     *      If there's any error in the communication between {@link Channel}s.
     */
    <V,T extends Throwable>
    V call(Callable<V,T> callable) throws IOException, T, InterruptedException;

    /**
     * Makes an asynchronous remote procedure call.
     *
     * <p>
     * Similar to {@link #call(Callable)} but returns immediately.
     * The result of the {@link Callable} can be obtained through the {@link Future} object.
     *
     * @return
     *      The {@link Future} object that can be used to wait for the completion.
     * @throws IOException
     *      If there's an error during the communication.
     */
    <V,T extends Throwable>
    Future<V> callAsync(final Callable<V,T> callable) throws IOException;

    /**
     * Performs an orderly shut down of this channel (and the remote peer.)
     *
     * @throws IOException
     *      if the orderly shut-down failed.
     */
    void close() throws IOException;

    /**
     * Waits for this {@link Channel} to be closed down.
     *
     * The close-down of a {@link Channel} might be initiated locally or remotely.
     *
     * @throws InterruptedException
     *      If the current thread is interrupted while waiting for the completion.
     * @since 1.300
     */
    public void join() throws InterruptedException;

    /**
     * Waits for this {@link Channel} to be closed down, but only up the given milliseconds.
     *
     * @throws InterruptedException
     *      If the current thread is interrupted while waiting for the completion.
     * @since 1.300
     */
    public void join(long timeout) throws InterruptedException;

    /**
     * Exports an object for remoting to the other {@link Channel}
     * by creating a remotable proxy.
     *
     * <p>
     * All the parameters and return values must be serializable.
     *
     * @param type
     *      Interface to be remoted.
     * @return
     *      the proxy object that implements <tt>T</tt>. This object can be transfered
     *      to the other {@link Channel}, and calling methods on it from the remote side
     *      will invoke the same method on the given local <tt>instance</tt> object.
     */
    <T> T export( Class<T> type, T instance);
}
