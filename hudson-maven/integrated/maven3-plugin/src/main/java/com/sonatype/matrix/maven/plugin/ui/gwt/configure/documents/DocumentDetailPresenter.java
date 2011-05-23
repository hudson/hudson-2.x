/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents;

import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.event.DocumentAddedEvent;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.event.DocumentSelectedEvent;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.event.DocumentUpdatedEvent;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.internal.DocumentDetailPresenterImpl;

/**
 * Manages the UI for editing a document.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@ImplementedBy(DocumentDetailPresenterImpl.class)
public interface DocumentDetailPresenter
    extends DocumentSelectedEvent.Handler, DocumentAddedEvent.Handler, DocumentUpdatedEvent.Handler
{
    DocumentDetailView getView();

    void setDocument(Document document);

    void doSave();

    void doCancel();
}
