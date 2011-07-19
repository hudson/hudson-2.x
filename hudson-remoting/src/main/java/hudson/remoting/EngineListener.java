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

package hudson.remoting;

/**
 * Receives status notification from {@link Engine}.
 *
 * The callback will be invoked on a non-GUI thread.
 *
 * @author Kohsuke Kawaguchi
 */
public interface EngineListener {
    /**
     * Status message that indicates the progress of the operation.
     */
    void status(String msg);

    /**
     * Status message, with additoinal stack trace that indicates an error that was recovered.
     */
    void status(String msg, Throwable t);

    /**
     * Fatal error that's non recoverable. 
     */
    void error(Throwable t);

    /**
     * Called when a connection is terminated.
     */
    void onDisconnect();
}
