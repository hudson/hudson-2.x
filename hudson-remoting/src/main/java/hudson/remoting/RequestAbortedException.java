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
 * Signals that the communication is aborted and thus
 * the pending {@link Request} will never recover its {@link Response}.
 *
 * @author Kohsuke Kawaguchi
 */
public class RequestAbortedException extends RuntimeException {
    public RequestAbortedException(Throwable cause) {
        super(cause);
    }
}
