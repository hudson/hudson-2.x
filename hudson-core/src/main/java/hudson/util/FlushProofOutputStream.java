/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link OutputStream} that blocks {@link #flush()} method.
 * @author Kohsuke Kawaguchi
 * @since 1.349
 */
public class FlushProofOutputStream extends DelegatingOutputStream {
    public FlushProofOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void flush() throws IOException {
    }
}

