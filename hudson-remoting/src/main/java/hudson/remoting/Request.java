/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.remoting;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Request/response pattern over {@link Channel}, the layer-1 service.
 *
 * <p>
 * This assumes that the receiving side has all the class definitions
 * available to de-serialize {@link Request}, just like {@link Command}.
 *
 * @author Kohsuke Kawaguchi
 * @see Response
 */
abstract class Request<RSP extends Serializable,EXC extends Throwable> extends Command {
    /**
     * Executed on a remote system to perform the task.
     *
     * @param channel
     *      The local channel. From the view point of the JVM that
     *      {@link #call(Channel) made the call}, this channel is
     *      the remote channel.
     * @return
     *      the return value will be sent back to the calling process.
     * @throws EXC
     *      The exception will be forwarded to the calling process.
     *      If no checked exception is supposed to be thrown, use {@link RuntimeException}.
     */
    protected abstract RSP perform(Channel channel) throws EXC;

    /**
     * Uniquely identifies this request.
     * Used for correlation between request and response.
     */
    private final int id;

    private volatile Response<RSP,EXC> response;

    /**
     * If this request performed some I/O back in the caller side during the remote call execution, set to last such
     * operation, so that we can block until its completion.
     */
    volatile transient Future<?> lastIo;

    /**
     * While executing the call this is set to the handle of the execution.
     */
    protected volatile transient Future<?> future;


    protected Request() {
        synchronized(Request.class) {
            id = nextId++;
        }
    }

    /**
     * Sends this request to a remote system, and blocks until we receives a response.
     *
     * @param channel
     *      The channel from which the request will be sent.
     * @throws InterruptedException
     *      If the thread is interrupted while it's waiting for the call to complete.
     * @throws IOException
     *      If there's an error during the communication.
     * @throws RequestAbortedException
     *      If the channel is terminated while the call is in progress.
     * @throws EXC
     *      If the {@link #perform(Channel)} throws an exception.
     */
    public final RSP call(Channel channel) throws EXC, InterruptedException, IOException {
        // Channel.send() locks channel, and there are other call sequences
        // (  like Channel.terminate()->Request.abort()->Request.onCompleted()  )
        // that locks channel -> request, so lock objects in the same order
        synchronized(channel) {
            synchronized(this) {
                response=null;

                channel.pendingCalls.put(id,this);
                channel.send(this);
            }
        }

        try {
            synchronized(this) {
                // set the thread name to represent the channel we are blocked on,
                // so that thread dump would give us more useful information.
                Thread t = Thread.currentThread();
                final String name = t.getName();
                try {
                    // wait until the response arrives
                    t.setName(name+" / waiting for "+channel);
                    while(response==null && !channel.isInClosed())
                        // I don't know exactly when this can happen, as pendingCalls are cleaned up by Channel,
                        // but in production I've observed that in rare occasion it can block forever, even after a channel
                        // is gone. So be defensive against that.
                        wait(30*1000);

                    if (response==null)
                        // channel is closed and we still don't have a response
                        throw new RequestAbortedException(null);
                } finally {
                    t.setName(name);
                }

                if (lastIo != null) {
                    try {
                        lastIo.get();
                    } catch (ExecutionException ignore) {
                    }
                }
                Object exc = response.exception;

                if (exc!=null) {
                    if(exc instanceof RequestAbortedException) {
                        // add one more exception, so that stack trace shows both who's waiting for the completion
                        // and where the connection outage was detected.
                        exc = new RequestAbortedException((RequestAbortedException)exc);
                    }
                    throw (EXC)exc; // some versions of JDK fails to compile this line. If so, upgrade your JDK.
                }

                return response.returnValue;
            }
        } catch (InterruptedException e) {
            // if we are cancelled, abort the remote computation, too.
            // do this outside the "synchronized(this)" block to prevent locking Request and Channel in a wrong order.
            synchronized (channel) { // ... so that the close check and send won't be interrupted in the middle by a close
                if (!channel.isOutClosed())
                    channel.send(new Cancel(id));   // only send a cancel if we can, or else ChannelClosedException will mask the original cause
            }
            throw e;
        }
    }

    /**
     * Makes an invocation but immediately returns without waiting for the completion
     * (AKA asynchronous invocation.)
     *
     * @param channel
     *      The channel from which the request will be sent.
     * @return
     *      The {@link Future} object that can be used to wait for the completion.
     * @throws IOException
     *      If there's an error during the communication.
     */
    public final hudson.remoting.Future<RSP> callAsync(final Channel channel) throws IOException {
        response=null;

        channel.pendingCalls.put(id,this);
        channel.send(this);

        return new hudson.remoting.Future<RSP>() {

            private volatile boolean cancelled;

            public boolean cancel(boolean mayInterruptIfRunning) {
                if (cancelled || isDone()) {
                    return false;
                }
                cancelled = true;
                if (mayInterruptIfRunning) {
                    try {
                        channel.send(new Cancel(id));
                    } catch (IOException x) {
                        return false;
                    }
                }
                return true;
            }

            public boolean isCancelled() {
                return cancelled;
            }

            public boolean isDone() {
                return isCancelled() || response!=null;
            }

            public RSP get() throws InterruptedException, ExecutionException {
                synchronized(Request.this) {
                    try {
                        while(response==null) {
                            if (isCancelled()) {
                                throw new CancellationException();
                            }
                            Request.this.wait(); // wait until the response arrives
                        }
                    } catch (InterruptedException e) {
                        try {
                            channel.send(new Cancel(id));
                        } catch (IOException e1) {
                            // couldn't cancel. ignore.
                        }
                        throw e;
                    }

                    if(response.exception!=null)
                        throw new ExecutionException(response.exception);

                    return response.returnValue;
                }
            }

            public RSP get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                synchronized(Request.this) {
                    if(response==null) {
                        if (isCancelled()) {
                            throw new CancellationException();
                        }
                        Request.this.wait(unit.toMillis(timeout)); // wait until the response arrives
                    }
                    if(response==null)
                        throw new TimeoutException();

                    if(response.exception!=null)
                        throw new ExecutionException(response.exception);

                    return response.returnValue;
                }
            }
        };
    }


    /**
     * Called by the {@link Response} when we received it.
     */
    /*package*/ synchronized void onCompleted(Response<RSP,EXC> response) {
        this.response = response;
        notify();
    }

    /**
     * Aborts the processing. The calling thread will receive an exception. 
     */
    /*package*/ void abort(IOException e) {
        onCompleted(new Response(id,new RequestAbortedException(e)));
    }

    /**
     * Schedules the execution of this request.
     */
    protected final void execute(final Channel channel) {
        channel.executingCalls.put(id,this);
        future = channel.executor.submit(new Runnable() {
            public void run() {
                try {
                    Command rsp;
                    CURRENT.set(Request.this);
                    try {
                        RSP r = Request.this.perform(channel);
                        // normal completion
                        rsp = new Response<RSP,EXC>(id,r);
                    } catch (Throwable t) {
                        // error return
                        rsp = new Response<RSP,Throwable>(id,t);
                    } finally {
                        CURRENT.set(null);
                    }
                    if(chainCause)
                        rsp.createdAt.initCause(createdAt);

                    synchronized (channel) {// expand the synchronization block of the send() method to a check
                        if(!channel.isOutClosed())
                            channel.send(rsp);
                    }
                } catch (IOException e) {
                    // communication error.
                    // this means the caller will block forever
                    logger.log(Level.SEVERE, "Failed to send back a reply",e);
                } finally {
                    channel.executingCalls.remove(id);
                }
            }
        });
    }

    /**
     * Next request ID.
     */
    private static int nextId=0;

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(Request.class.getName());

    /**
     * Set to true to chain {@link Command#createdAt} to track request/response relationship.
     * This will substantially increase the network traffic, but useful for debugging.
     */
    public static boolean chainCause = Boolean.getBoolean(Request.class.getName()+".chainCause");

    /**
     * Set to the {@link Request} object during {@linkplain #perform(Channel) the execution of the call}.
     */
    static ThreadLocal<Request> CURRENT = new ThreadLocal<Request>();

    static int getCurrentRequestId() {
        Request r = CURRENT.get();
        return r!=null ? r.id : 0;
    }

    /**
     * Interrupts the execution of the remote computation.
     */
    private static final class Cancel extends Command {
        private final int id;

        Cancel(int id) {
            this.id = id;
        }

        protected void execute(Channel channel) {
            Request<?,?> r = channel.executingCalls.get(id);
            if(r==null)     return; // already completed
            Future<?> f = r.future;
            if(f!=null)     f.cancel(true);
        }
    }
}
