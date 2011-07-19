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
 * Model object used to display the generic error when Hudson start up fails fatally during initialization.
 *
 * <p>
 * <tt>index.jelly</tt> would display a nice friendly error page.
 *
 * @author Kohsuke Kawaguchi
 */
public class HudsonFailedToLoad extends ErrorObject {
    public final Throwable exception;

    public HudsonFailedToLoad(Throwable exception) {
        this.exception = exception;
    }

    public String getStackTrace() {
        return Functions.printThrowable(exception);
    }
}
