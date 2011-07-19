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

import hudson.remoting.Channel;
import hudson.remoting.PingThread;
import hudson.remoting.Channel.Mode;
import hudson.util.ChunkedOutputStream;
import hudson.util.ChunkedInputStream;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Builds a {@link Channel} on top of two HTTP streams (one used for each direction.)
 *
 * @author Kohsuke Kawaguchi
 */
abstract class FullDuplexHttpChannel {
    private Channel channel;

    private InputStream upload;

    private final UUID uuid;
    private final boolean restricted;

    private boolean completed;

    public FullDuplexHttpChannel(UUID uuid, boolean restricted) throws IOException {
        this.uuid = uuid;
        this.restricted = restricted;
    }

    /**
     * This is where we send the data to the client.
     *
     * <p>
     * If this connection is lost, we'll abort the channel.
     */
    public synchronized void download(StaplerRequest req, StaplerResponse rsp) throws InterruptedException, IOException {
        rsp.setStatus(HttpServletResponse.SC_OK);

        // server->client channel.
        // this is created first, and this controls the lifespan of the channel
        rsp.addHeader("Transfer-Encoding", "chunked");
        OutputStream out = rsp.getOutputStream();
        if (DIY_CHUNKING) out = new ChunkedOutputStream(out);

        // send something out so that the client will see the HTTP headers
        out.write("Starting HTTP duplex channel".getBytes());
        out.flush();

        // wait until we have the other channel
        while(upload==null)
            wait();

        try {
            channel = new Channel("HTTP full-duplex channel " + uuid,
                    Computer.threadPoolForRemoting, Mode.BINARY, upload, out, null, restricted);

            // so that we can detect dead clients, periodically send something
            PingThread ping = new PingThread(channel) {
                @Override
                protected void onDead() {
                    LOGGER.info("Duplex-HTTP session " + uuid + " is terminated");
                    // this will cause the channel to abort and subsequently clean up
                    try {
                        upload.close();
                    } catch (IOException e) {
                        // this can never happen
                        throw new AssertionError(e);
                    }
                }
            };
            ping.start();
            main(channel);
            channel.join();
            ping.interrupt();
        } finally {
            // publish that we are done
            completed=true;
            notify();
        }
    }

    protected abstract void main(Channel channel) throws IOException, InterruptedException;

    /**
     * This is where we receive inputs from the client.
     */
    public synchronized void upload(StaplerRequest req, StaplerResponse rsp) throws InterruptedException, IOException {
        rsp.setStatus(HttpServletResponse.SC_OK);
        InputStream in = req.getInputStream();
        if(DIY_CHUNKING)    in = new ChunkedInputStream(in);

        // publish the upload channel
        upload = in;
        notify();

        // wait until we are done
        while (!completed)
            wait();
    }

    public Channel getChannel() {
        return channel;
    }

    private static final Logger LOGGER = Logger.getLogger(FullDuplexHttpChannel.class.getName());

    /**
     * Set to true if the servlet container doesn't support chunked encoding.
     */
    public static boolean DIY_CHUNKING = Boolean.getBoolean("hudson.diyChunking");
}
