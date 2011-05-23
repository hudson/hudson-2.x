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
import com.google.gwt.view.client.HasData;
import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.internal.DocumentMasterViewImpl;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.workspace.WorkspaceView;

/**
 * Provides the UI for editing documents.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@ImplementedBy(DocumentMasterViewImpl.class)
public interface DocumentMasterView
    extends WorkspaceView
{
    @DefaultLocale("en_US")
    interface MessagesResource
        extends Messages
    {
        @DefaultMessage("Documents")
        String documents();

        @DefaultMessage("Refresh")
        String refresh();

        @DefaultMessage("Add")
        String add();

        @DefaultMessage("Remove")
        String remove();

        @DefaultMessage("ID")
        String id();

        @DefaultMessage("Type")
        String type();

        @DefaultMessage("Name")
        String name();

        @DefaultMessage("Please add or select a document.")
        String detailSummary();
    }

    void setPresenter(DocumentMasterPresenter presenter);

    HasData<Document> getDocumentDataContainer();

    void setDocumentDetailView(DocumentDetailView view);

    void setDocumentSelected(boolean flag);

    void scrollToNewDocument();
}
