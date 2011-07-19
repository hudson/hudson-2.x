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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Hex-binary decoding stream.
 *
 * @author Kohsuke Kawaguchi
 * @see EncodingStream
 */
public class DecodingStream extends FilterOutputStream {
    private int last = -1;

    public DecodingStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) throws IOException {
        if(last==-1) {
            last = b;
            return;
        }

        out.write( Character.getNumericValue(last)*16 + Character.getNumericValue(b) );
        last = -1;
    }
}
