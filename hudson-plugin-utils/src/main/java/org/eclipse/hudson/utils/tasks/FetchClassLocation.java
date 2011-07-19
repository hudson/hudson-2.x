/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.utils.tasks;

import hudson.FilePath;
import hudson.remoting.Callable;
import hudson.remoting.Which;

/**
 * Fetches the jar file reference in which the given class is located.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class FetchClassLocation
    implements Callable<FilePath,Exception>
{
    private final String className;

    public FetchClassLocation(final String className) {
        assert className != null;
        this.className = className;
    }

    public FetchClassLocation(final Class type) {
        // assert type != null;
        this(type.getCanonicalName());
    }

    public FilePath call() throws Exception {
        Class type = Class.forName(className);
        return new FilePath(Which.jarFile(type));
    }
}
