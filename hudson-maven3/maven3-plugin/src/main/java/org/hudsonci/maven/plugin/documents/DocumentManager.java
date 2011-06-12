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

package org.hudsonci.maven.plugin.documents;

import com.google.inject.ImplementedBy;
import org.hudsonci.maven.model.config.DocumentDTO;
import org.hudsonci.maven.model.config.DocumentTypeDTO;

import java.util.Collection;

import org.hudsonci.maven.plugin.documents.internal.DocumentManagerImpl;

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
