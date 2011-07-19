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
 * Hex-binary encoding stream.
 *
 * TODO: use base64binary.
 *
 * @author Kohsuke Kawaguchi
 * @see DecodingStream
 */
public class EncodingStream extends FilterOutputStream {
    public EncodingStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(chars.charAt((b >> 4) & 0xF));
        out.write(chars.charAt(b & 0xF));
    }

    private static final String chars = "0123456789ABCDEF";
}
