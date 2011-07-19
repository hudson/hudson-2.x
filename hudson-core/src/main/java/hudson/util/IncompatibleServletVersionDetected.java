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

import hudson.remoting.Which;

import java.io.IOException;
import java.net.URL;

/**
 * Model object used to display the error top page if
 * we find out that the container doesn't support servlet 2.4.
 *
 * <p>
 * <tt>index.jelly</tt> would display a nice friendly error page.
 *
 * @author Kohsuke Kawaguchi
 */
public class IncompatibleServletVersionDetected extends ErrorObject {
    private final Class servletClass;

    public IncompatibleServletVersionDetected(Class servletClass) {
        this.servletClass = servletClass;
    }
    
    public URL getWhereServletIsLoaded() throws IOException {
        return Which.jarURL(servletClass);
    }
}
