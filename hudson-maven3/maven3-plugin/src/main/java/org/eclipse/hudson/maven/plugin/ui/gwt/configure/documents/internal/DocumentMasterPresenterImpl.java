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

package org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.internal;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import org.eclipse.hudson.gwt.common.confirmdialog.ConfirmDialogPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.Document;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.DocumentDetailPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.DocumentManagerLiaison;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.DocumentMasterPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.DocumentMasterView;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.event.DocumentAddedEvent;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.event.DocumentSelectedEvent;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.event.DocumentUpdatedEvent;

import javax.inject.Inject;
import javax.inject.Singleton;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link DocumentMasterPresenter}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Singleton
public class DocumentMasterPresenterImpl
    implements DocumentMasterPresenter
{
    private final DocumentMasterView view;

    private final EventBus eventBus;

    private final DocumentManagerLiaison documentManagerLiaison;

    private final DocumentDetailPresenter documentDetailPresenter;

    private final ConfirmDialogPresenter confirmDialogPresenter;

    private final MessagesResource messages;

    private final SingleSelectionModel<Document> selectionModel;

    @Inject
    public DocumentMasterPresenterImpl(final DocumentMasterView view,
                                       final EventBus eventBus,
                                       final Scheduler scheduler,
                                       final DocumentManagerLiaison documentManagerLiaison,
                                       final DocumentDetailPresenter documentDetailPresenter,
                                       final ConfirmDialogPresenter confirmDialogPresenter,
                                       final MessagesResource messages)
    {
        this.view = checkNotNull(view);
        this.eventBus = checkNotNull(eventBus);
        this.documentManagerLiaison = checkNotNull(documentManagerLiaison);
        this.documentDetailPresenter = checkNotNull(documentDetailPresenter);
        this.confirmDialogPresenter = checkNotNull(confirmDialogPresenter);
        this.messages = checkNotNull(messages);

        view.setPresenter(this);

        // Hook up data provider
        HasData<Document> documentDataContainer = view.getDocumentDataContainer();
        selectionModel = new SingleSelectionModel<Document>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler()
        {
            public void onSelectionChange(final SelectionChangeEvent event) {
                Document document = selectionModel.getSelectedObject();
                fireDocumentSelected(document);
                view.setDocumentSelected(true);
            }
        });
        documentDataContainer.setSelectionModel(selectionModel);
        documentManagerLiaison.getDataProvider().addDataDisplay(documentDataContainer);

        // Attach the detail view, handle events
        view.setDocumentDetailView(documentDetailPresenter.getView());
        eventBus.addHandler(DocumentSelectedEvent.TYPE, documentDetailPresenter);
        eventBus.addHandler(DocumentAddedEvent.TYPE, documentDetailPresenter);
        eventBus.addHandler(DocumentUpdatedEvent.TYPE, documentDetailPresenter);

        // Fetch the initial data
        scheduler.scheduleDeferred(new ScheduledCommand()
        {
            public void execute() {
                documentManagerLiaison.fetchAll();
            }
        });
    }

    public DocumentMasterView getView() {
        return view;
    }

    private void fireDocumentSelected(final Document document) {
        eventBus.fireEvent(new DocumentSelectedEvent(document));
    }

    public void doRefresh() {
        // Re-fetch all documents and force the detail to cancel
        documentDetailPresenter.doCancel();
        documentManagerLiaison.fetchAll();
    }

    public void doAdd() {
        // Create a new blank document and select it
        Document document = documentManagerLiaison.create();
        selectionModel.setSelected(document, true);

        // FIXME: This *almost* works... scrolls to the item before last, dunno how to fix ATM :-(
        // FIXME: The cell table doesn't yet know about the new item, not sure how to hook it up to a proper event to deal either
        view.scrollToNewDocument();
    }

    public void doRemove() {
        final Document document = selectionModel.getSelectedObject();
        assert document != null;

        confirmDialogPresenter.confirm(
            messages.removeTitle(),
            messages.removeMessage(document.getDisplayName()),
            new ConfirmDialogPresenter.OkCancelCallback()
            {
                public void onOk() {
                    documentDetailPresenter.setDocument(null);
                    documentManagerLiaison.remove(document);
                    view.setDocumentSelected(false);
                }

                public void onCancel() {
                    // nothing to do
                }
            });
    }
}
