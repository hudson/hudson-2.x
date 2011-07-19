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

package org.eclipse.hudson.legacy.maven.agent;

import java.io.OutputStream;
import java.io.IOException;
import java.io.FilterOutputStream;

/**
 * JDK's {@link FilterOutputStream} has some real issues. 
 *
 * @author Kohsuke Kawaguchi
 */
class RealFilterOutputStream extends FilterOutputStream {
    public RealFilterOutputStream(OutputStream core) {
        super(core);
    }

    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void close() throws IOException {
        out.close();
    }
}
