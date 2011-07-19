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

import hudson.util.FileVisitor;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link FileVisitor} that creates a zip archive.
 *
 * @see ArchiverFactory#ZIP
 */
final class ZipArchiver extends Archiver {
    private final byte[] buf = new byte[8192];
    private final ZipOutputStream zip;

    ZipArchiver(OutputStream out) {
        zip = new ZipOutputStream(out);
        zip.setEncoding(System.getProperty("file.encoding"));
    }

    public void visit(File f, String relativePath) throws IOException {
        if(f.isDirectory()) {
            ZipEntry dirZipEntry = new ZipEntry(relativePath+'/');
            // Setting this bit explicitly is needed by some unzipping applications (see HUDSON-3294).
            dirZipEntry.setExternalAttributes(BITMASK_IS_DIRECTORY);
            zip.putNextEntry(dirZipEntry);
            zip.closeEntry();
        } else {
            zip.putNextEntry(new ZipEntry(relativePath));
            FileInputStream in = new FileInputStream(f);
            int len;
            while((len=in.read(buf))>0)
                zip.write(buf,0,len);
            in.close();
            zip.closeEntry();
        }
        entriesWritten++;
    }

    public void close() throws IOException {
        zip.close();
    }

    // Bitmask indicating directories in 'external attributes' of a ZIP archive entry.
    private static final long BITMASK_IS_DIRECTORY = 1<<4;
}
