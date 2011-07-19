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
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link Writer} that spools the output and writes to another {@link Writer} later.
 *
 * @author Kohsuke Kawaguchi
 * @deprecated since 2008-05-28. moved to stapler
 */
public final class CharSpool extends Writer {
    private List<char[]> buf;

    private char[] last = new char[1024];
    private int pos;

    public void write(char cbuf[], int off, int len) {
        while(len>0) {
            int sz = Math.min(last.length-pos,len);
            System.arraycopy(cbuf,off,last,pos,sz);
            len -= sz;
            off += sz;
            pos += sz;
            renew();
        }
    }

    private void renew() {
        if(pos<last.length)
            return;

        if(buf==null)
            buf = new LinkedList<char[]>();
        buf.add(last);
        last = new char[1024];
        pos = 0;
    }

    public void write(int c) {
        renew();
        last[pos++] = (char)c;
    }

    public void flush() {
        // noop
    }

    public void close() {
        // noop
    }

    public void writeTo(Writer w) throws IOException {
        if(buf!=null) {
            for (char[] cb : buf) {
                w.write(cb);
            }
        }
        w.write(last,0,pos);
    }
}
