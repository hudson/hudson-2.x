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

import java.io.IOException;

/**
 * Model object used to display the error top page if
 * there appears to be no temporary directory.
 *
 * <p>
 * <tt>index.jelly</tt> would display a nice friendly error page.
 *
 * @author Kohsuke Kawaguchi
 */
public class NoTempDir extends ErrorObject {
    public final IOException exception;

    public NoTempDir(IOException exception) {
        this.exception = exception;
    }

    public String getStackTrace() {
        return Functions.printThrowable(exception);
    }

    public String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }
}
