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

package org.eclipse.hudson.utils.tasks;

import hudson.remoting.Callable;
import hudson.remoting.Channel;
import hudson.remoting.RemoteInputStream;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.SocketInputStream;
import hudson.remoting.SocketOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Opens a server socket on a node and facilitates accepting connections.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class OpenServerSocket
    implements Callable<OpenServerSocket.Acceptor,IOException>
{
    private static final Logger log = LoggerFactory.getLogger(OpenServerSocket.class);

    public static final int DEFAULT_SO_TIMEOUT = 30 * 1000;

    /**
     * Used to accept a new connection on the remote server socket.
     */
    public interface Acceptor
        extends Closeable
    {
        int getPort();

        Connection accept(boolean close) throws IOException;

        Connection accept() throws IOException;
    }

    /**
     * Represents the accepted connection.
     */
    public interface Connection
        extends Closeable
    {
        InputStream getInput();

        OutputStream getOutput();
    }

    public Acceptor call() throws IOException {
        return new AcceptorImpl();
    }

    protected void customize(final ServerSocket serverSocket) throws SocketException {
        assert serverSocket != null;
        serverSocket.setSoTimeout(DEFAULT_SO_TIMEOUT);
    }

    // TODO: Document behavior on both sides of channel so its easier to comprehend whats going on here

    private class AcceptorImpl
        implements Acceptor, Serializable
    {
        private static final long serialVersionUID = 1L;

        private transient final ServerSocket serverSocket;

        public AcceptorImpl() throws IOException {
            serverSocket = new ServerSocket();
            serverSocket.bind(null);
            customize(serverSocket);
            log.debug("Created acceptor");
        }

        public Connection accept(final boolean close) throws IOException {
            log.debug("Accepting");
            Socket socket = serverSocket.accept();
            log.debug("Accepted: {}", socket);
            if (close) {
                close();
            }
            return new ConnectionImpl(socket);
        }

        public Connection accept() throws IOException {
            return accept(false);
        }

        public void close() throws IOException {
            log.debug("Closing");
            serverSocket.close();
        }

        public int getPort() {
            return serverSocket.getLocalPort();
        }

        /**
         * Executed on remote, returns a proxy.
         */
        private Object writeReplace() {
            return Channel.current().export(Acceptor.class, this);
        }
    }

    private class ConnectionImpl
        implements Connection, Serializable
    {
        private static final long serialVersionUID = 1L;

        private InputStream input;

        private OutputStream output;

        public ConnectionImpl(final InputStream input, final OutputStream output) {
            assert input != null;
            this.input = input;
            assert output != null;
            this.output = output;
            log.debug("Created connection");
        }

        private ConnectionImpl(final Socket socket) throws IOException {
            // assert socket != null
            this(new SocketInputStream(socket), new SocketOutputStream(socket));
            // TODO: How to close the socket?
        }

        public InputStream getInput() {
            return input;
        }

        public OutputStream getOutput() {
            return output;
        }

        public void close() throws IOException {
            // FIXME: nop for now, trying to close the streams causes problems
        }

        /**
         * Executed on remote, returns connection wrapper with remote streams.
         */
        private Object writeReplace() {
            return new ConnectionImpl(new RemoteInputStream(input), new RemoteOutputStream(output));
        }

        /**
         * Re-establishes buffering.
         */
        private Object readResolve() {
            this.input = new BufferedInputStream(input);
            this.output = new BufferedOutputStream(output);
            return this;
        }
    }
}
