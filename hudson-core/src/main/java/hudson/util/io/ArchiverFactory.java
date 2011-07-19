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

import hudson.FilePath.TarCompression;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Creates {@link Archiver} on top of a stream.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.359
*/
public abstract class ArchiverFactory implements Serializable {
    /**
     * Creates an archiver on top of the given stream.
     */
    public abstract Archiver create(OutputStream out) throws IOException;

    /**
     * Uncompressed tar format.
     */
    public static ArchiverFactory TAR = new TarArchiverFactory(TarCompression.NONE);

    /**
     * tar+gz
     */
    public static ArchiverFactory TARGZ = new TarArchiverFactory(TarCompression.GZIP);

    /**
     * Zip format.
     */
    public static ArchiverFactory ZIP = new ZipArchiverFactory();



    private static final class TarArchiverFactory extends ArchiverFactory {
        private final TarCompression method;

        private TarArchiverFactory(TarCompression method) {
            this.method = method;
        }

        public Archiver create(OutputStream out) throws IOException {
            return new TarArchiver(method.compress(out));
        }

        private static final long serialVersionUID = 1L;
    }

    private static final class ZipArchiverFactory extends ArchiverFactory {
        public Archiver create(OutputStream out) {
            return new ZipArchiver(out);
        }

        private static final long serialVersionUID = 1L;
    }

    private static final long serialVersionUID = 1L;
}
