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

package hudson.remoting;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Thread that copies a stream.
 * 
 * @author Kohsuke Kawaguchi
 */
class Copier extends Thread {
    private final InputStream in;
    private final OutputStream out;

    public Copier(String threadName, InputStream in, OutputStream out) {
        super(threadName);
        this.in = in;
        this.out = out;
    }

    public void run() {
        try {
            byte[] buf = new byte[8192];
            int len;
            while((len=in.read(buf))>0)
                out.write(buf,0,len);
            in.close();
        } catch (IOException e) {
            // TODO: what to do?
        }
    }
}
