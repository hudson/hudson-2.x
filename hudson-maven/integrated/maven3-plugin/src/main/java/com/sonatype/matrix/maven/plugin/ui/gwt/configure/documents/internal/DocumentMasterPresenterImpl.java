/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.internal;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sonatype.matrix.gwt.common.confirmdialog.ConfirmDialogPresenter;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.Document;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.DocumentDetailPresenter;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.DocumentManagerLiaison;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.DocumentMasterPresenter;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.DocumentMasterView;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.event.DocumentAddedEvent;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.event.DocumentSelectedEvent;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.event.DocumentUpdatedEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link DocumentMasterPresenter}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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
            @Override
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

    @Override
    public DocumentMasterView getView() {
        return view;
    }

    private void fireDocumentSelected(final Document document) {
        eventBus.fireEvent(new DocumentSelectedEvent(document));
    }

    @Override
    public void doRefresh() {
        // Re-fetch all documents and force the detail to cancel
        documentDetailPresenter.doCancel();
        documentManagerLiaison.fetchAll();
    }

    @Override
    public void doAdd() {
        // Create a new blank document and select it
        Document document = documentManagerLiaison.create();
        selectionModel.setSelected(document, true);

        // FIXME: This *almost* works... scrolls to the item before last, dunno how to fix ATM :-(
        // FIXME: The cell table doesn't yet know about the new item, not sure how to hook it up to a proper event to deal either
        view.scrollToNewDocument();
    }

    @Override
    public void doRemove() {
        final Document document = selectionModel.getSelectedObject();
        assert document != null;

        confirmDialogPresenter.confirm(
            messages.removeTitle(),
            messages.removeMessage(document.getDisplayName()),
            new ConfirmDialogPresenter.OkCancelCallback()
            {
                @Override
                public void onOk() {
                    documentDetailPresenter.setDocument(null);
                    documentManagerLiaison.remove(document);
                    view.setDocumentSelected(false);
                }

                @Override
                public void onCancel() {
                    // nothing to do
                }
            });
    }
}
