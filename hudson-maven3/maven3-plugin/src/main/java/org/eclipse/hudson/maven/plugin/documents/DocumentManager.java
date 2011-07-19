/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.eclipse.hudson.maven.plugin.documents;

import com.google.inject.ImplementedBy;

import org.eclipse.hudson.maven.plugin.documents.internal.DocumentManagerImpl;
import org.eclipse.hudson.maven.model.config.DocumentDTO;
import org.eclipse.hudson.maven.model.config.DocumentTypeDTO;

import java.util.Collection;


/**
 * Provides access to configuration documents.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@ImplementedBy(DocumentManagerImpl.class)
public interface DocumentManager
{
    // TODO: Consider using ID interface (hudson-util) and expose String -> ID factory method here for translation

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
