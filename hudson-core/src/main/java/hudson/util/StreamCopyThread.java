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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@link Thread} that copies {@link InputStream} to {@link OutputStream}.
 *
 * @author Kohsuke Kawaguchi
 */
public class StreamCopyThread extends Thread {
    private final InputStream in;
    private final OutputStream out;
    private final boolean closeOut;

    public StreamCopyThread(String threadName, InputStream in, OutputStream out, boolean closeOut) {
        super(threadName);
        this.in = in;
        if (out == null) {
            throw new NullPointerException("out is null");
        }
        this.out = out;
        this.closeOut = closeOut;
    }

    public StreamCopyThread(String threadName, InputStream in, OutputStream out) {
        this(threadName,in,out,false);
    }

    @Override
    public void run() {
        try {
            try {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);
            } finally {
                // it doesn't make sense not to close InputStream that's already EOF-ed,
                // so there's no 'closeIn' flag.
                in.close();
                if(closeOut)
                    out.close();
            }
        } catch (IOException e) {
            // TODO: what to do?
        }
    }
}
