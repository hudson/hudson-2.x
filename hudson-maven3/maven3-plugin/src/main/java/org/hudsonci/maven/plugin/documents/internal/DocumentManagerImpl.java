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

package org.hudsonci.maven.plugin.documents.internal;

import org.hudsonci.utils.common.Iso8601Date;
import org.hudsonci.maven.model.config.DocumentDTO;
import org.hudsonci.maven.model.config.DocumentTypeDTO;
import org.hudsonci.service.SecurityService;

import hudson.model.User;

import org.hudsonci.maven.plugin.documents.DocumentException;
import org.hudsonci.maven.plugin.documents.DocumentManager;
import org.hudsonci.maven.plugin.documents.DocumentNotFoundException;
import org.hudsonci.maven.plugin.documents.DocumentStore;
import org.hudsonci.maven.plugin.documents.DuplicateDocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hudsonci.maven.model.config.DocumentStandardAttributeDTO.CREATED;
import static org.hudsonci.maven.model.config.DocumentStandardAttributeDTO.CREATED_BY;
import static org.hudsonci.maven.model.config.DocumentStandardAttributeDTO.UPDATED;
import static org.hudsonci.maven.model.config.DocumentStandardAttributeDTO.UPDATED_BY;
import static hudson.model.Hudson.ADMINISTER;

/**
 * Default implementation of {@link DocumentManager}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class DocumentManagerImpl
    implements DocumentManager
{
    private static final Logger log = LoggerFactory.getLogger(DocumentManagerImpl.class);

    private final Map<UUID, DocumentDTO> documents = new LinkedHashMap<UUID, DocumentDTO>();

    private final DocumentStore store;

    private final SecurityService security;

    @Inject
    public DocumentManagerImpl(final DocumentStore store, final SecurityService security) {
        this.store = checkNotNull(store);
        this.security = checkNotNull(security);

        // Prime our document set. At the moment, we don't treat this as a cache, but the record, store only used for persistence
        try {
            for (DocumentDTO document : store.loadAll()) {
                validate(document);
                documents.put(idFor(document), document);
            }
        }
        catch (IOException e) {
            throw new DocumentException(e);
        }
    }

    String getCurrentUserId() {
        User user = security.getCurrentUser();
        if (user == null) {
            user = security.getUnknownUser();
        }
        return user.getId();
    }

    /**
     * Returns an ISO 8601 formatted date for right now.
     */
    String now() {
        return Iso8601Date.format(new Date());
    }

    String randomId() {
        return UUID.randomUUID().toString();
    }

    UUID idFor(final String id) {
        checkNotNull(id);
        return UUID.fromString(id);
    }

    UUID idFor(final DocumentDTO document) {
        checkNotNull(document);
        return idFor(document.getId());
    }

    void validate(final DocumentDTO document) {
        checkNotNull(document);
        checkNotNull(document.getId(), "Missing document ID");
        idFor(document.getId()); // Ensure ID is formatted correctly
        checkNotNull(document.getType(), "Missing document type");
    }

    DocumentDTO copyOf(final DocumentDTO document) {
        checkNotNull(document);
        return new DocumentDTO()
            .withId(document.getId())
            .withType(document.getType())
            .withName(document.getName())
            .withDescription(document.getDescription())
            .withAttributes(document.getAttributes())
            .withContent(document.getContent());
    }

    DocumentDTO summaryOf(final DocumentDTO document) {
        return copyOf(document).withContent(null);
    }

    public synchronized Collection<DocumentDTO> getDocuments(final boolean summary) {        
        log.debug("Get documents w/summary={}", summary);
        security.checkPermission(ADMINISTER);

        List<DocumentDTO> result = new ArrayList<DocumentDTO>(documents.size());
        for (DocumentDTO document : documents.values()) {
            result.add(summary ? summaryOf(document) : document);
        }

        return result;
    }

    public synchronized Collection<DocumentDTO> getDocuments(final DocumentTypeDTO type, final boolean summary) {
        checkNotNull(type);
        log.debug("Get documents w/type={}, w/summary={}", type, summary);
        security.checkPermission(ADMINISTER);

        List<DocumentDTO> result = new ArrayList<DocumentDTO>();
        for (DocumentDTO document : documents.values()) {
            if (type.equals(document.getType())) {
                result.add(summary ? summaryOf(document) : document);
            }
        }

        return result;
    }

    public synchronized DocumentDTO addDocument(final DocumentDTO document) {
        checkNotNull(document);
        log.debug("Adding document: {}", document);
        security.checkPermission(ADMINISTER);

        // If the new document does not have an ID, then generate one for it
        if (document.getId() == null) {
            String id = randomId();
            document.setId(id);
            log.debug("Generated ID for new document: {}", id);
        }

        validate(document);

        UUID id = idFor(document);
        if (documents.containsKey(id)) {
            throw new DuplicateDocumentException(id);
        }

        document.setAttribute(CREATED, now());
        document.setAttribute(CREATED_BY, getCurrentUserId());

        try {
            store.store(document);
        }
        catch (IOException e) {
            throw new DocumentException(e);
        }

        documents.put(id, document);

        return summaryOf(document);
    }

    private synchronized DocumentDTO getDocument(final UUID id, final boolean summary) {
        checkNotNull(id);
        log.debug("Get document w/id={}, w/summary={}", id, summary);
        security.checkPermission(ADMINISTER);

        DocumentDTO document = documents.get(id);
        if (document == null) {
            throw new DocumentNotFoundException(id);
        }

        return summary ? summaryOf(document) : document;
    }

    public synchronized DocumentDTO getDocument(final String id, final boolean summary) {
        return getDocument(idFor(id), summary);
    }

    public synchronized DocumentDTO updateDocument(final DocumentDTO document) {
        checkNotNull(document);
        log.debug("Update document: {}", document);
        security.checkPermission(ADMINISTER);

        validate(document);

        UUID id = idFor(document);
        if (!documents.containsKey(id)) {
            throw new DocumentNotFoundException(id);
        }

        document.setAttribute(UPDATED, now());
        document.setAttribute(UPDATED_BY, getCurrentUserId());

        try {
            store.store(document);
        }
        catch (IOException e) {
            throw new DocumentException(e);
        }

        documents.put(id, document);

        return summaryOf(document);
    }

    private synchronized void removeDocument(final UUID id) {
        checkNotNull(id);
        log.debug("Remove document w/id={}", id);
        security.checkPermission(ADMINISTER);

        DocumentDTO document = documents.remove(id);
        if (document == null) {
            throw new DocumentNotFoundException(id);
        }
        try {
            store.delete(document);
        }
        catch (IOException e) {
            throw new DocumentException(e);
        }
    }

    public synchronized void removeDocument(final String id) {
        removeDocument(idFor(id));
    }
}
