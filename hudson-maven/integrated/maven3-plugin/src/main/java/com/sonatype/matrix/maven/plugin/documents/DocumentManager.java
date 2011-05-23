/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.documents;

import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.model.config.DocumentDTO;
import com.sonatype.matrix.maven.model.config.DocumentTypeDTO;
import com.sonatype.matrix.maven.plugin.documents.internal.DocumentManagerImpl;

import java.util.Collection;

/**
 * Provides access to configuration documents.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@ImplementedBy(DocumentManagerImpl.class)
public interface DocumentManager
{
    // TODO: Consider using ID interface (matrix-common) and expose String -> ID factory method here for translation

    Collection<DocumentDTO> getDocuments(boolean summary);

    Collection<DocumentDTO> getDocuments(DocumentTypeDTO type, boolean summary);

    /**
     * Add a document to the document store.
     * 
     * @param document the document to add
     * @return a summary of the document added
     */
    DocumentDTO addDocument(DocumentDTO document);

    DocumentDTO getDocument(String id, boolean summary);

    /**
     * Update the specified document by id.
     * 
     * @param document
     * @return a summary of the updated document
     */
    DocumentDTO updateDocument(DocumentDTO document);

    void removeDocument(String id);
}
