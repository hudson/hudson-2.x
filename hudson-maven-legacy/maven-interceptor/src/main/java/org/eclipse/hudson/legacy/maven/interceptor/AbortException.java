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

package org.eclipse.hudson.legacy.maven.interceptor;

/**
 * Thrown when {@link PluginManagerListener} returned false to orderly
 * abort the execution. The caller shouldn't dump the stack trace for
 * this exception.
 */
public final class AbortException extends RuntimeException {
    public AbortException(String message) {
        super(message);
    }
    public AbortException(String message, Exception e) {
        super(message, e);
    }
}
