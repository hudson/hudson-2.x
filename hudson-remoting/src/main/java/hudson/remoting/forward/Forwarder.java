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

import java.io.Serializable;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Abstracts away how the forwarding is set up.
 *
 * @author Kohsuke Kawaguchi
*/
public interface Forwarder extends Serializable {
    /**
     * Establishes a port forwarding connection and returns
     * the writer end.
     *
     * @param out
     *      The writer end to the initiator. The callee will
     *      start a thread that writes to this.
     */
    OutputStream connect(OutputStream out) throws IOException;
}
