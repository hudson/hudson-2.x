/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.internal.DocumentManagerLiaisonImpl;

/**
 * Provides access to the {@link com.sonatype.matrix.maven.plugin.documents.DocumentManager}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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
