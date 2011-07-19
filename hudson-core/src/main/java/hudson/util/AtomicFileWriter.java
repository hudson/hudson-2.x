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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Buffered {@link FileWriter} that uses UTF-8.
 *
 * <p>
 * The write operation is atomic when used for overwriting;
 * it either leaves the original file intact, or it completely rewrites it with new contents.
 *
 * @author Kohsuke Kawaguchi
 */
public class AtomicFileWriter extends Writer {

    private final Writer core;
    private final File tmpFile;
    private final File destFile;

    /**
     * Writes with UTF-8 encoding.
     */
    public AtomicFileWriter(File f) throws IOException {
        this(f,"UTF-8");
    }

    /**
     * @param encoding
     *      File encoding to write. If null, platform default encoding is chosen.
     */
    public AtomicFileWriter(File f, String encoding) throws IOException {
        File dir = f.getParentFile();
        try {
            dir.mkdirs();
            tmpFile = File.createTempFile("atomic",null, dir);
        } catch (IOException e) {
            throw new IOException2("Failed to create a temporary file in "+ dir,e);
        }
        destFile = f;
        if (encoding==null)
            encoding = Charset.defaultCharset().name();
        core = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile),encoding));
    }

    @Override
    public void write(int c) throws IOException {
        core.write(c);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        core.write(str,off,len);
    }

    public void write(char cbuf[], int off, int len) throws IOException {
        core.write(cbuf,off,len);
    }

    public void flush() throws IOException {
        core.flush();
    }

    public void close() throws IOException {
        core.close();
    }

    /**
     * When the write operation failed, call this method to
     * leave the original file intact and remove the temporary file.
     * This method can be safely invoked from the "finally" block, even after
     * the {@link #commit()} is called, to simplify coding.
     */
    public void abort() throws IOException {
        close();
        tmpFile.delete();
    }

    public void commit() throws IOException {
        close();
        if(destFile.exists() && !destFile.delete()) {
            tmpFile.delete();
            throw new IOException("Unable to delete "+destFile);
        }
        tmpFile.renameTo(destFile);
    }

    @Override
    protected void finalize() throws Throwable {
        // one way or the other, temporary file should be deleted.
        tmpFile.delete();
    }

    /**
     * Until the data is committed, this file captures
     * the written content.
     */
    public File getTemporaryFile() {
        return tmpFile;
    }
}
