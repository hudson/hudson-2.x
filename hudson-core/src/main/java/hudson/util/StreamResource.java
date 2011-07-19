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

import org.apache.tools.ant.types.Resource;

import java.io.InputStream;
import java.io.IOException;

/**
 * Wraps {@link InputStream} to {@link Resource}.
 * @author Kohsuke Kawaguchi
 */
public class StreamResource extends Resource {
    private final InputStream in;

    /**
     * @param name
     *      Used for display purpose.
     */
    public StreamResource(String name, InputStream in) {
        this.in = in;
        setName(name);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return in;
    }
}
