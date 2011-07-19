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

import java.io.OutputStream;

/**
 * @author Kohsuke Kawaguchi
 */
public final class NullStream extends OutputStream {
    public NullStream() {}

    @Override
    public void write(byte b[]) {
    }

    @Override
    public void write(byte b[], int off, int len) {
    }

    public void write(int b) {
    }
}
