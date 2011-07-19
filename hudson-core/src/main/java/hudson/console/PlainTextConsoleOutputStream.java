/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.console;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Filters out console notes.
 *
 * @author Kohsuke Kawaguchi
 */
public class PlainTextConsoleOutputStream extends LineTransformationOutputStream {
    private final OutputStream out;

    /**
     *
     */
    public PlainTextConsoleOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * Called after we read the whole line of plain text.
     */
    protected void eol(byte[] in, int sz) throws IOException {

        int next = ConsoleNote.findPreamble(in,0,sz);

        // perform byte[]->char[] while figuring out the char positions of the BLOBs
        int written = 0;
        while (next>=0) {
            if (next>written) {
                out.write(in,written,next-written);
                written = next;
            } else {
                assert next==written;
            }

            int rest = sz - next;
            ByteArrayInputStream b = new ByteArrayInputStream(in, next, rest);

            ConsoleNote.skip(new DataInputStream(b));

            int bytesUsed = rest - b.available(); // bytes consumed by annotations
            written += bytesUsed;


            next = ConsoleNote.findPreamble(in,written,sz-written);
        }
        // finish the remaining bytes->chars conversion
        out.write(in,written,sz-written);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
        out.close();
    }


    private static final Logger LOGGER = Logger.getLogger(ConsoleAnnotationOutputStream.class.getName());
}
