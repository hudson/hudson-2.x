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

package hudson.util;

import hudson.Functions;

/**
 * Model object used to display the error top page if
 * we find that we don't have enough permissions to run.
 *
 * <p>
 * <tt>index.jelly</tt> would display a nice friendly error page.
 *
 * @author Kohsuke Kawaguchi
 */
public class InsufficientPermissionDetected extends ErrorObject {
    //TODO: review and check whether we can do it private
    public final SecurityException exception;

    public SecurityException getException() {
        return exception;
    }

    public InsufficientPermissionDetected(SecurityException e) {
        this.exception = e;
    }

    public String getExceptionTrace() {
        return Functions.printThrowable(exception);
    }
}
