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

package hudson;

import hudson.util.DelegatingOutputStream;

import java.io.OutputStream;

/**
 * {@link OutputStream} that blocks {@link #close()} method.
 * @author Kohsuke Kawaguchi
 */
public class CloseProofOutputStream extends DelegatingOutputStream {
    public CloseProofOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void close() {
    }
}
