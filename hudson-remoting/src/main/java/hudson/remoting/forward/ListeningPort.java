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
 *
 *******************************************************************************/ 

package hudson.remoting.forward;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents a listening port that forwards a connection
 * via port forwarding.
 *
 * @author Kohsuke Kawaguchi
 */
public interface ListeningPort extends Closeable {
    /**
     * TCP/IP port that is listening.
     */
    int getPort();

    /**
     * Shuts down the port forwarding by removing the server socket.
     * Connections that are already established will not be affected
     * by this operation.
     */
    void close() throws IOException;
}
