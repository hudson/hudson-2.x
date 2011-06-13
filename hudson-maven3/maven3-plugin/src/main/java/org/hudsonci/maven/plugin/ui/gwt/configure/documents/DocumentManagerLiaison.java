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

package org.hudsonci.maven.plugin.ui.gwt.configure.documents;

import org.hudsonci.maven.plugin.ui.gwt.configure.documents.internal.DocumentManagerLiaisonImpl;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.inject.ImplementedBy;

/**
 * Provides access to the {@link org.hudsonci.maven.plugin.documents.DocumentManager}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@ImplementedBy(DocumentManagerLiaisonImpl.class)
public interface DocumentManagerLiaison
{
    @DefaultLocale("en_US")
    interface MessagesResource
        extends Messages
    {
        @DefaultMessage("Failed to fetch all documents")
        String documentFetchAllFailed();

        @DefaultMessage("Failed to add document")
        String documentAddFailed();

        @DefaultMessage("Failed to update document")
        String documentUpdateFailed();

        @DefaultMessage("Failed to remove document")
        String documentRemoveFailed();
    }

    /**
     * Get the data-provider for document data.
     */
    AbstractDataProvider<Document> getDataProvider();

    /**
     * Fetch all documents.
     */
    void fetchAll();

    /**
     * Create a new temporal document.
     */
    Document create();

    /**
     * Add or update the document based on its state.
     */
    void save(Document document);

    /**
     * Add a new document.
     */
    void add(Document document);

    /**
     * Update an existing document.
     */
    void update(Document document);

    /**
     * Remove an existing document.
     */
    void remove(Document document);
}
