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

package hudson.remoting.forward;

import hudson.remoting.VirtualChannel;
import hudson.remoting.Callable;
import hudson.remoting.SocketInputStream;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.SocketOutputStream;
import hudson.remoting.Channel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Creates {@link Forwarder}.
 *
 * @author Kohsuke Kawaguchi
 */
public class ForwarderFactory {
    /**
     * Creates a connector on the remote side that connects to the speicied host and port.
     */
    public static Forwarder create(VirtualChannel channel, final String remoteHost, final int remotePort) throws IOException, InterruptedException {
        return channel.call(new Callable<Forwarder,IOException>() {
            public Forwarder call() throws IOException {
                return new ForwarderImpl(remoteHost,remotePort);
            }

            private static final long serialVersionUID = 1L;
        });
    }

    public static Forwarder create(String remoteHost, int remotePort) {
        return new ForwarderImpl(remoteHost,remotePort);
    }

    private static class ForwarderImpl implements Forwarder {
        private final String remoteHost;
        private final int remotePort;

        private ForwarderImpl(String remoteHost, int remotePort) {
            this.remoteHost = remoteHost;
            this.remotePort = remotePort;
        }

        public OutputStream connect(OutputStream out) throws IOException {
            Socket s = new Socket(remoteHost, remotePort);
            new CopyThread(String.format("Copier to %s:%d", remoteHost, remotePort),
                new SocketInputStream(s), out).start();
            return new RemoteOutputStream(new SocketOutputStream(s));
        }

        /**
         * When sent to the remote node, send a proxy.
         */
        private Object writeReplace() {
            return Channel.current().export(Forwarder.class, this);
        }
    }
}
