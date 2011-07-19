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

package hudson.util.io;

import hudson.Functions;
import hudson.org.apache.tools.tar.TarOutputStream;
import hudson.util.FileVisitor;
import hudson.util.IOException2;
import org.apache.tools.tar.TarEntry;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

import static org.apache.tools.tar.TarConstants.LF_SYMLINK;

/**
 * {@link FileVisitor} that creates a tar archive.
 *
 * @see ArchiverFactory#TAR
 */
final class TarArchiver extends Archiver {
    private final byte[] buf = new byte[8192];
    private final TarOutputStream tar;

    TarArchiver(OutputStream out) {
        tar = new TarOutputStream(new BufferedOutputStream(out) {
            // TarOutputStream uses TarBuffer internally,
            // which flushes the stream for each block. this creates unnecessary
            // data stream fragmentation, and flush request to a remote, which slows things down.
            @Override
            public void flush() throws IOException {
                // so don't do anything in flush
            }
        });
        tar.setLongFileMode(TarOutputStream.LONGFILE_GNU);
    }

    @Override
    public void visitSymlink(File link, String target, String relativePath) throws IOException {
        TarEntry e = new TarEntry(relativePath, LF_SYMLINK);

        try {
            StringBuffer linkName = (StringBuffer) LINKNAME_FIELD.get(e);
            linkName.setLength(0);
            linkName.append(target);
        } catch (IllegalAccessException x) {
            throw new IOException2("Failed to set linkName", x);
        }

        tar.putNextEntry(e);
        entriesWritten++;
    }

    @Override
    public boolean understandsSymlink() {
        return true;
    }

    public void visit(File file, String relativePath) throws IOException {
        if(Functions.isWindows())
            relativePath = relativePath.replace('\\','/');

        if(file.isDirectory())
            relativePath+='/';
        TarEntry te = new TarEntry(relativePath);
        te.setModTime(file.lastModified());
        if(!file.isDirectory())
            te.setSize(file.length());

        tar.putNextEntry(te);

        if (!file.isDirectory()) {
            FileInputStream in = new FileInputStream(file);
            try {
                int len;
                while((len=in.read(buf))>=0)
                    tar.write(buf,0,len);
            } finally {
                in.close();
            }
        }

        tar.closeEntry();
        entriesWritten++;
    }

    public void close() throws IOException {
        tar.close();
    }

    private static final Field LINKNAME_FIELD = getTarEntryLinkNameField();

    private static Field getTarEntryLinkNameField() {
        try {
            Field f = TarEntry.class.getDeclaredField("linkName");
            f.setAccessible(true);
            return f;
        } catch (SecurityException e) {
            throw new AssertionError(e);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }
}
