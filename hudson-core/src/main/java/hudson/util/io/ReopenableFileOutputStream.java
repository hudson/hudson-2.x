/*******************************************************************************
 *
 * Copyright (c) 2010, CloudBees, Inc.
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
 *******************************************************************************/ 

package hudson.util.io;

import hudson.util.IOException2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link OutputStream} that writes to a file.
 *
 * <p>
 * Unlike regular {@link FileOutputStream}, this implementation allows the caller to close,
 * and then keep writing.
 *
 * @author Kohsuke Kawaguchi
 */
public class ReopenableFileOutputStream extends OutputStream {
    private final File out;

    private OutputStream current;
    private boolean appendOnNextOpen = false;

    public ReopenableFileOutputStream(File out) {
        this.out = out;
    }

    private synchronized OutputStream current() throws IOException {
        if (current==null)
            try {
                current = new FileOutputStream(out,appendOnNextOpen);
            } catch (FileNotFoundException e) {
                throw new IOException2("Failed to open "+out,e);
            }
        return current;
    }

    @Override
    public void write(int b) throws IOException {
        current().write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        current().write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        current().write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        current().flush();
    }

    @Override
    public synchronized void close() throws IOException {
        if (current!=null) {
            current.close();
            appendOnNextOpen = true;
            current = null;
        }
    }

    /**
     * In addition to close, ensure that the next "open" would truncate the file.
     */
    public synchronized void rewind() throws IOException {
        close();
        appendOnNextOpen = false;
    }
}
