/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

package org.hudsonci.maven.eventspy.common;

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
