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

import hudson.remoting.RemoteOutputStream;
import hudson.remoting.SocketOutputStream;
import hudson.remoting.SocketInputStream;
import hudson.remoting.VirtualChannel;
import hudson.remoting.Callable;
import hudson.remoting.Channel;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Closeable;
import static java.util.logging.Level.FINE;
import java.util.logging.Logger;

/**
 * Port forwarder over a remote channel.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.315
 */
public class PortForwarder extends Thread implements Closeable, ListeningPort {
    private final Forwarder forwarder;
    private final ServerSocket socket;

    public PortForwarder(int localPort, Forwarder forwarder) throws IOException {
        super(String.format("Port forwarder %d",localPort));
        this.forwarder = forwarder;
        this.socket = new ServerSocket(localPort);
        // mark as a daemon thread by default.
        // the caller can explicitly cancel this by doing "setDaemon(false)"
        setDaemon(true);
    }

    public int getPort() {
        return socket.getLocalPort();
    }

    @Override
    public void run() {
        try {
            try {
                while(true) {
                    final Socket s = socket.accept();
                    new Thread("Port forwarding session from "+s.getRemoteSocketAddress()) {
                        public void run() {
                            try {
                                final OutputStream out = forwarder.connect(new RemoteOutputStream(new SocketOutputStream(s)));
                                new CopyThread("Copier for "+s.getRemoteSocketAddress(),
                                    new SocketInputStream(s), out).start();
                            } catch (IOException e) {
                                // this happens if the socket connection is terminated abruptly.
                                LOGGER.log(FINE,"Port forwarding session was shut down abnormally",e);
                            }
                        }
                    }.start();
                }
            } finally {
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.log(FINE,"Port forwarding was shut down abnormally",e);
        }
    }

    /**
     * Shuts down this port forwarder.
     */
    public void close() throws IOException {
        interrupt();
        socket.close();
    }

    /**
     * Starts a {@link PortForwarder} accepting remotely at the given channel,
     * which connects by using the given connector.
     *
     * @return
     *      A {@link Closeable} that can be used to shut the port forwarding down.
     */
    public static ListeningPort create(VirtualChannel ch, final int acceptingPort, Forwarder forwarder) throws IOException, InterruptedException {
        // need a remotable reference
        final Forwarder proxy = ch.export(Forwarder.class, forwarder);

        return ch.call(new Callable<ListeningPort,IOException>() {
            public ListeningPort call() throws IOException {
                PortForwarder t = new PortForwarder(acceptingPort, proxy);
                t.start();
                return Channel.current().export(ListeningPort.class,t);
            }
        });
    }

    private static final Logger LOGGER = Logger.getLogger(PortForwarder.class.getName());
}
