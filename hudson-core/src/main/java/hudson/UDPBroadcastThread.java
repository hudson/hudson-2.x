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

package hudson;

import hudson.model.Hudson;
import hudson.util.OneShotEvent;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedByInterruptException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Monitors a UDP multicast broadcast and respond with the location of the Hudson service.
 *
 * <p>
 * Useful for auto-discovery of Hudson in the network.
 *
 * @author Kohsuke Kawaguchi
 */
public class UDPBroadcastThread extends Thread {
    private final Hudson hudson;
    //TODO: review and check whether we can do it private
    public final OneShotEvent ready = new OneShotEvent();
    private MulticastSocket mcs;
    private boolean shutdown;

    public UDPBroadcastThread(Hudson hudson) throws IOException {
        super("Hudson UDP "+PORT+" monitoring thread");
        this.hudson = hudson;
        mcs = new MulticastSocket(PORT);
    }

    public OneShotEvent getReady() {
        return ready;
    }

    @Override
    public void run() {
        try {
            mcs.joinGroup(MULTICAST);
            ready.signal();

            while(true) {
                byte[] buf = new byte[2048];
                DatagramPacket p = new DatagramPacket(buf,buf.length);
                mcs.receive(p);

                SocketAddress sender = p.getSocketAddress();

                // prepare a response
                TcpSlaveAgentListener tal = hudson.getTcpSlaveAgentListener();

                StringBuilder rsp = new StringBuilder("<hudson>");
                tag(rsp,"version",Hudson.VERSION);
                tag(rsp,"url",hudson.getRootUrl());
                tag(rsp,"slave-port",tal==null?null:tal.getPort());

                for (UDPBroadcastFragment f : UDPBroadcastFragment.all())
                    f.buildFragment(rsp,sender);

                rsp.append("</hudson>");

                byte[] response = rsp.toString().getBytes("UTF-8");
                mcs.send(new DatagramPacket(response,response.length,sender));
            }
        } catch (ClosedByInterruptException e) {
            // shut down
        } catch (BindException e) {
            // if we failed to listen to UDP, just silently abandon it, as a stack trace
            // makes people unnecessarily concerned, for a feature that currently does no good.
            LOGGER.log(Level.WARNING, "Failed to listen to UDP port "+PORT,e);
        } catch (IOException e) {
            if (shutdown)   return; // forcibly closed
            LOGGER.log(Level.WARNING, "UDP handling problem",e);
        }
    }

    private void tag(StringBuilder buf, String tag, Object value) {
        if(value==null) return;
        buf.append('<').append(tag).append('>').append(value).append("</").append(tag).append('>');
    }

    public void shutdown() {
        shutdown = true;
        mcs.close();
        interrupt();
    }

    public static final int PORT = Integer.getInteger("hudson.udp",33848);

    private static final Logger LOGGER = Logger.getLogger(UDPBroadcastThread.class.getName());

    /**
     * Multicast socket address.
     */
    public static InetAddress MULTICAST;

    static {
        try {
            MULTICAST = InetAddress.getByAddress(new byte[]{(byte)239, (byte)77, (byte)124, (byte)213});
        } catch (UnknownHostException e) {
            throw new Error(e);
        }
    }
}
