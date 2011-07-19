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

package org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents;

import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.event.DocumentAddedEvent;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.event.DocumentSelectedEvent;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.event.DocumentUpdatedEvent;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.internal.DocumentDetailPresenterImpl;

import com.google.inject.ImplementedBy;

/**
 * Manages the UI for editing a document.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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
