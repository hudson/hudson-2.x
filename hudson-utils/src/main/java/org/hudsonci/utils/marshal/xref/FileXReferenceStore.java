/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.utils.marshal.xref;

import org.hudsonci.utils.id.OID;
import org.hudsonci.utils.io.Closer;
import org.hudsonci.utils.marshal.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple file-based {@link XReference} storage strategy.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class FileXReferenceStore
    implements XReferenceStore
{
    protected static final Logger log = LoggerFactory.getLogger(FileXReferenceStore.class);

    protected final Marshaller marshaller;

    protected final File root;

    public FileXReferenceStore(final Marshaller marshaller, final File root) {
        this.marshaller = checkNotNull(marshaller);
        // root may be null
        this.root = root;
    }

    public FileXReferenceStore(final Marshaller marshaller) {
        this(marshaller, null);
    }

    public File getRoot() {
        return root;
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

    protected void mkdirs(final File file) {
        assert file != null;
        File dir = file.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                log.warn("Failed to create directory: " + dir);
            }
            else {
                log.debug("Created directory: {}", dir);
            }
        }
        else if (dir.isFile()) {
            log.warn("Expected directory; found file: {}", dir);
        }
    }

    protected String sanitize(final String path) {
        assert path != null;
        // ugly but this is how hudson handles $ in a path
        return path.replaceAll("\\$", "_-");
    }

    protected File getFile(final XReference ref) {
        assert ref != null;
        String path = sanitize(ref.getPath());
        File dir = getRoot();
        return (dir != null) ? new File(dir, path) : new File(path);
    }

    protected Marshaller getMarshaller(final XReference ref) {
        Marshaller m = ref.getMarshaller();
        if (m == null) {
            m = marshaller;
        }
        return m;
    }

    public void store(final XReference ref) throws IOException {
        assert ref != null;

        Marshaller marshaller = getMarshaller(ref);
        File file = getFile(ref);
        mkdirs(file);

        if (log.isTraceEnabled()) {
            log.trace("Marshalling reference: {} to file: {}", OID.get(ref), file);
        }

        Writer writer = new BufferedWriter(new FileWriter(file));
        try {
            marshaller.marshal(ref.get(), writer);
        }
        finally {
            Closer.close(writer);
        }
    }

    public Object load(final XReference ref) throws IOException {
        assert ref != null;

        Object value;
        Marshaller marshaller = getMarshaller(ref);
        File file = getFile(ref);

        if (log.isTraceEnabled()) {
            log.trace("Unmarshalling reference: {} from file: {}", OID.get(ref), file);
        }

        Reader reader = new BufferedReader(new FileReader(file));
        try {
            value = marshaller.unmarshal(reader);
        }
        finally {
            Closer.close(reader);
        }

        return value;
    }
}
