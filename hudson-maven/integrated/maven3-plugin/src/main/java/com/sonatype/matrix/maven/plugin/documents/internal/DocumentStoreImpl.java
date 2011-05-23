/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.documents.internal;

import com.sonatype.matrix.common.io.Closer;
import com.sonatype.matrix.common.marshal.Marshaller;
import com.sonatype.matrix.common.marshal.XStreamMarshaller;
import com.sonatype.matrix.maven.model.config.DocumentDTO;
import com.sonatype.matrix.maven.plugin.documents.DocumentStore;
import com.sonatype.matrix.service.SystemService;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * File-based XStream marshaled {@link DocumentDTO} store.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Singleton
public class DocumentStoreImpl
    implements DocumentStore
{
    private static final Logger log = LoggerFactory.getLogger(DocumentStoreImpl.class);

    public static final String ROOT_PATH = "maven/documents";

    public static final String FILE_SUFFIX = ".xml";

    private File rootDir;

    private Marshaller marshaller;

    @Inject
    public DocumentStoreImpl(final SystemService system) {
        checkNotNull(system);

        XStream xs = new XStream();
        xs.setClassLoader(getClass().getClassLoader());
        xs.processAnnotations(DocumentDTO.class);
        setMarshaller(new XStreamMarshaller(xs));

        setRootDir(new File(system.getWorkingDirectory(), ROOT_PATH));
    }

    public File getRootDir() {
        return rootDir;
    }

    public void setRootDir(final File dir) {
        this.rootDir = checkNotNull(dir);
        log.debug("Root directory: {}", dir);
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

    public void setMarshaller(final Marshaller marshaller) {
        this.marshaller = checkNotNull(marshaller);
    }

    File fileFor(final String id) {
        checkNotNull(id);
        return new File(rootDir, String.format("%s%s", id, FILE_SUFFIX));
    }

    File fileFor(final UUID id) {
        checkNotNull(id);
        return fileFor(id.toString());
    }

    File fileFor(final DocumentDTO document) {
        checkNotNull(document);
        return fileFor(document.getId());
    }

    @Override
    public Collection<DocumentDTO> loadAll() throws IOException {
        FilenameFilter filter = new FilenameFilter()
        {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(FILE_SUFFIX);
            }
        };

        List<DocumentDTO> documents = new ArrayList<DocumentDTO>();
        Set<String> ids = new HashSet<String>();
        if (rootDir.exists()) {
            log.debug("Loading documents from directory: {}", rootDir);

            for (File file : rootDir.listFiles(filter)) {
                DocumentDTO document = load(file);

                String id = document.getId();
                if (ids.contains(id)) {
                    log.warn("Duplicate document ID detected: {}", id);
                }
                ids.add(id);

                documents.add(document);
            }
        }

        return documents;
    }

    @Override
    public boolean contains(final UUID id) {
        checkNotNull(id);
        return fileFor(id).exists();
    }

    private DocumentDTO load(final File file) throws FileNotFoundException {
        checkNotNull(file);

        log.debug("Loading document from file: {}", file);

        if (!file.exists()) {
            throw new FileNotFoundException("Document file missing: " + file);
        }

        Reader reader = new BufferedReader(new FileReader(file));
        try {
            DocumentDTO document = (DocumentDTO) marshaller.unmarshal(reader);

            // Sanity check content should be non-null, complain if that is not the case, but continue
            if (document.getContent() == null) {
                log.warn("Loaded document with no content");
            }

            return document;
        }
        finally {
            Closer.close(reader);
        }
    }

    @Override
    public DocumentDTO load(final UUID id) throws IOException {
        checkNotNull(id);
        return load(fileFor(id));
    }

    @Override
    public void store(final DocumentDTO document) throws IOException {
        checkNotNull(document);
        File file = fileFor(document);

        log.debug("Storing document to file: {}", file);

        // Sanity check content should be non-null, complain if that is not the case, but continue
        if (document.getContent() == null) {
            log.warn("Storing document with no content");
        }

        File dir = file.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory structure for document file: " + file);
        }

        Writer writer = new BufferedWriter(new FileWriter(file));
        try {
            marshaller.marshal(document, writer);
        }
        finally {
            Closer.close(writer);
        }
    }

    @Override
    public void delete(final DocumentDTO document) throws IOException {
        checkNotNull(document);
        File file = fileFor(document);

        log.debug("Deleting document file: {}", file);

        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("Failed to delete document file: " + file);
            }
        }
        else {
            log.warn("Ignoring delete; document file missing: {}", file);
        }
    }
}
