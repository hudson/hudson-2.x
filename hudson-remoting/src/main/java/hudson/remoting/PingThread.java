/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
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
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Periodically perform a ping.
 *
 * <p>
 * Useful when a connection needs to be kept alive by sending data,
 * or when the disconnection is not properly detected.
 *
 * <p>
 * {@link #onDead()} method needs to be overrided to define
 * what to do when a connection appears to be dead.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.170
 */
public abstract class PingThread extends Thread {
    private final Channel channel;

    /**
     * Time out in milliseconds.
     * If the response doesn't come back by then, the channel is considered dead.
     */
    private final long timeout;

    /**
     * Performs a check every this milliseconds.
     */
    private final long interval;

    public PingThread(Channel channel, long timeout, long interval) {
        super("Ping thread for channel "+channel);
        this.channel = channel;
        this.timeout = timeout;
        this.interval = interval;
        setDaemon(true);
    }

    public PingThread(Channel channel, long interval) {
        this(channel, 4*60*1000/*4 mins*/, interval);
    }

    public PingThread(Channel channel) {
        this(channel,10*60*1000/*10 mins*/);
    }

    public void run() {
        try {
            while(true) {
                long nextCheck = System.currentTimeMillis()+interval;

                ping();

                // wait until the next check
                long diff;
                while((diff=nextCheck-System.currentTimeMillis())>0)
                    Thread.sleep(diff);
            }
        } catch (ChannelClosedException e) {
            LOGGER.fine(getName()+" is closed. Terminating");
        } catch (IOException e) {
            onDead();
        } catch (InterruptedException e) {
            // use interruption as a way to terminate the ping thread.
            LOGGER.fine(getName()+" is interrupted. Terminating");
        }
    }

    private void ping() throws IOException, InterruptedException {
        Future<?> f = channel.callAsync(new Ping());
        try {
            f.get(timeout,MILLISECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RequestAbortedException)
                return; // connection has shut down orderly.
            onDead();
        } catch (TimeoutException e) {
            onDead();
        }
    }

    /**
     * Called when ping failed.
     */
    protected abstract void onDead();

    private static final class Ping implements Callable<Void, IOException> {
        private static final long serialVersionUID = 1L;

        public Void call() throws IOException {
            return null;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(PingThread.class.getName());
}
