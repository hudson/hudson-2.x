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
import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.internal.DocumentMasterPresenterImpl;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.workspace.WorkspacePresenter;

/**
 * Manages the display for editing documents.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@ImplementedBy(DocumentMasterPresenterImpl.class)
public interface DocumentMasterPresenter
    extends WorkspacePresenter
{
    @DefaultLocale("en_US")
    interface MessagesResource
        extends Messages
    {
        @DefaultMessage("Remove document ?")
        String removeTitle();

        @DefaultMessage("Remove document: {0} ?")
        String removeMessage(String context);
    }

    DocumentMasterView getView();

    void doRefresh();

    void doAdd();

    void doRemove();
}
