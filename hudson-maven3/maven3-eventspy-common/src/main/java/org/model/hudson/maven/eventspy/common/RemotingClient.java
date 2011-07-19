/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.model.hudson.maven.eventspy.common;

import hudson.remoting.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: Move to common, once common is small/util classes that can be put into the slavebundle

/**
 * Starts a remoting channel over TCP/IP.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class RemotingClient
    implements Closeable
{
    private static final Logger log = LoggerFactory.getLogger(RemotingClient.class);

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final int port;

    private Channel channel;

    public RemotingClient(final int port) {
        this.port = port;
    }

    public Channel getChannel() {
        ensureOpened();
        return channel;
    }

    public boolean isOpen() {
        return channel != null;
    }

    private void ensureOpened() {
        if (!isOpen()) {
            throw new IllegalStateException();
        }
    }

    public void open() throws IOException, InterruptedException {
        if (isOpen()) {
            throw new IllegalStateException();
        }

        log.debug("Opening w/port: {}", port);

        final Socket socket = new Socket((String)null, port);

        InputStream input = new BufferedInputStream(new FilterInputStream(socket.getInputStream())
        {
            public void close() throws IOException {
                socket.shutdownInput();
            }
        });

        OutputStream output = new BufferedOutputStream(new FilterOutputStream(socket.getOutputStream())
        {
            public void close() throws IOException {
                socket.shutdownOutput();
            }
        });

        channel = new Channel(getClass().getName(), executor, Channel.Mode.BINARY, input, output);

        log.debug("Opened");
    }

    public void join() throws InterruptedException {
        log.debug("Joining");

        getChannel().join();

        log.debug("Joined");
    }

    public void close() throws IOException {
        if (channel != null) {
            log.debug("Closing");

            channel.close();
            channel = null;

            log.debug("Closed");
        }
    }

    @Override
    public String toString() {
        return "RemotingClient{" +
            "port=" + port +
            ", channel=" + channel +
            '}';
    }

    /**
     * Copied from hudson's maven-agent, apparently the std impl causes problems?
     */
    private static class FilterOutputStream
        extends java.io.FilterOutputStream
    {
        public FilterOutputStream(final OutputStream out) {
            super(out);
        }

        public void write(final byte[] b) throws IOException {
            out.write(b);
        }

        public void write(final byte[] b, final int off, final int len) throws IOException {
            out.write(b, off, len);
        }

        public void close() throws IOException {
            out.close();
        }
    }

    /**
     * Command-line access.
     */
    public static void main(final String[] args) throws Exception {
        assert args != null;
        assert args.length == 1;

        int port = Integer.parseInt(args[0]);

        RemotingClient client = new RemotingClient(port);

        client.open();
        client.join();
        client.close();

        System.exit(0);
    }
}
