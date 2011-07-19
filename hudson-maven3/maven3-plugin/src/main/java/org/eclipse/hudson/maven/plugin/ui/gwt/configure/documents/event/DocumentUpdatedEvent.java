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

package org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.event;

import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.Document;

import com.google.gwt.event.shared.EventHandler;

/**
 * Event fired when a document update has completed successfully.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class DocumentUpdatedEvent
    extends DocumentEventSupport<DocumentUpdatedEvent.Handler>
{
    public static final Type<Handler> TYPE = new Type<Handler>();

    public DocumentUpdatedEvent(final Document document) {
        super(TYPE, document);
    }

    @Override
    protected void dispatch(final Handler handler) {
        handler.onDocumentUpdated(this);
    }

    public static interface Handler
        extends EventHandler
    {
        void onDocumentUpdated(DocumentUpdatedEvent event);
    }
}
