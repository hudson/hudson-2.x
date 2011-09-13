/*******************************************************************************
 *
 * Copyright (c) 2004-2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Winston Prakash
 *     
 *
 *******************************************************************************/
package org.eclipse.hudson.war.executable;

import java.net.URL;
import java.security.ProtectionDomain;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Simple class to make the war executable
 * @author Winston Prakash
 */
public class StartJetty {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("port", "8080"));

        Server server = new Server(port);
        SocketConnector connector = new SocketConnector();

        ProtectionDomain protectionDomain = StartJetty.class.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();

        WebAppContext context = new WebAppContext();

        context.setContextPath("/");
        context.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
        context.setServer(server);
        context.setWar(location.toExternalForm());

        server.addHandler(context);
        server.start();
        server.join();
    }
}
