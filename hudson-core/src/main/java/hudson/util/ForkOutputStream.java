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

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Kohsuke Kawaguchi
 */
public class ForkOutputStream extends OutputStream {
    private final OutputStream lhs;
    private final OutputStream rhs;

    public ForkOutputStream(OutputStream lhs, OutputStream rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void write(int b) throws IOException {
        lhs.write(b);
        rhs.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        lhs.write(b);
        rhs.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        lhs.write(b, off, len);
        rhs.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        lhs.flush();
        rhs.flush();
    }

    @Override
    public void close() throws IOException {
        lhs.close();
        rhs.close();
    }
}
