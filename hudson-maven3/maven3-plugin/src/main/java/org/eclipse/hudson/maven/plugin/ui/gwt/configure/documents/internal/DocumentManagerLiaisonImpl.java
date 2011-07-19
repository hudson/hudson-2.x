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

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import org.eclipse.hudson.maven.model.config.DocumentAttributeDTO;
import org.eclipse.hudson.maven.model.config.DocumentDTO;
import org.eclipse.hudson.maven.model.config.DocumentTypeDTO;
import org.eclipse.hudson.maven.model.config.DocumentsDTO;
import org.eclipse.hudson.gwt.common.restygwt.ServiceFailureNotifier;
import org.eclipse.hudson.gwt.common.waitdialog.WaitPresenter;
import org.eclipse.hudson.gwt.icons.silk.SilkIcons;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.Document;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.DocumentManagerLiaison;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.event.DocumentAddedEvent;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.event.DocumentRemovedEvent;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.event.DocumentUpdatedEvent;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.hudson.gwt.common.UUID.uuid;

/**
 * Default implementation of {@link DocumentManagerLiaison}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Singleton
public class DocumentManagerLiaisonImpl
    implements DocumentManagerLiaison
{
    private final EventBus eventBus;

    private final DocumentRestService documentRestService;

    private final DocumentDataProvider documentDataProvider;

    private final SilkIcons icons;

    private final WaitPresenter waitWidget;

    private final MessagesResource messages;

    private final ServiceFailureNotifier serviceFailureNotifier;

    @Inject
    public DocumentManagerLiaisonImpl(final EventBus eventBus,
                                      final DocumentRestService documentRestService,
                                      final DocumentDataProvider documentDataProvider,
                                      final SilkIcons icons,
                                      final WaitPresenter waitWidget,
                                      final ServiceFailureNotifier serviceFailureNotifier,
                                      final MessagesResource messages)
    {
        this.eventBus = checkNotNull(eventBus);
        this.documentRestService = checkNotNull(documentRestService);
        this.documentDataProvider = checkNotNull(documentDataProvider);
        this.icons = checkNotNull(icons);
        this.waitWidget = checkNotNull(waitWidget);
        this.serviceFailureNotifier = checkNotNull(serviceFailureNotifier);
        this.messages = checkNotNull(messages);
    }

    public DocumentDataProvider getDataProvider() {
        return documentDataProvider;
    }

    private List<Document> wrap(final List<DocumentDTO> source) {
        assert source != null;
        List<Document> target = new ArrayList<Document>(source.size());
        for (DocumentDTO item : source) {
            target.add(wrap(item));
        }
        return target;
    }

    private Document wrap(final DocumentDTO source) {
        assert source != null;
        return new DocumentImpl(source);
    }

    public void fetchAll() {
        Log.debug("Fetching all documents");

        waitWidget.startWaiting();

        // FIXME: Here should only need to fetch document summary... update once we can pull content as needed
        documentRestService.getDocuments(false, new MethodCallback<DocumentsDTO>()
        {
            public void onSuccess(final Method method, final DocumentsDTO result) {
                Log.debug("Received response to all documents; status=" + method.getResponse().getStatusText());
                documentDataProvider.set(wrap(result.getDocuments()));
                waitWidget.stopWaiting();
            }

            public void onFailure(final Method method, final Throwable exception) {
                serviceFailureNotifier.displayFailure(messages.documentFetchAllFailed(), method, exception);
            }
        });
    }

    public Document create() {
        DocumentImpl document = new DocumentImpl();
        documentDataProvider.add(document);
        Log.debug("Created new document: " + document);
        return document;
    }

    public void save(final Document document) {
        checkNotNull(document);

        if (document.isNew()) {
            add(document);
        }
        else {
            update(document);
        }
    }

    public void add(final Document document) {
        checkNotNull(document);

        if (Log.isDebugEnabled()) {
            Log.debug("Adding document: " + document);
        }
        final DocumentDTO data = ((DocumentImpl) document).get();

        waitWidget.startWaiting();

        documentRestService.addDocument(data, new MethodCallback<DocumentDTO>()
        {
            public void onSuccess(final Method method, final DocumentDTO result) {
                Log.debug("Added document: " + document);
                ((DocumentImpl) document).updateAttributes(result);
                ((DocumentImpl) document).setNew(false);
                documentDataProvider.refresh();
                fireDocumentAdded(document);
                waitWidget.stopWaiting();
            }

            public void onFailure(final Method method, final Throwable exception) {
                serviceFailureNotifier.displayFailure(messages.documentAddFailed(), method, exception);
            }
        });
    }

    private void fireDocumentAdded(final Document document) {
        eventBus.fireEvent(new DocumentAddedEvent(document));
    }

    public void update(final Document document) {
        checkNotNull(document);

        if (Log.isDebugEnabled()) {
            Log.debug("Updating document: " + document);
        }
        final DocumentDTO data = ((DocumentImpl) document).get();

        waitWidget.startWaiting();

        documentRestService.updateDocument(data.getId(), data, new MethodCallback<DocumentDTO>()
        {
            public void onSuccess(final Method method, final DocumentDTO result) {
                Log.debug("Updated document: " + document);
                ((DocumentImpl) document).updateAttributes(result);
                ((DocumentImpl) document).setDirty(false);
                documentDataProvider.refresh();
                fireDocumentUpdated(document);
                waitWidget.stopWaiting();
            }

            public void onFailure(final Method method, final Throwable exception) {
                serviceFailureNotifier.displayFailure(messages.documentUpdateFailed(), method, exception);
            }
        });
    }

    private void fireDocumentUpdated(final Document document) {
        eventBus.fireEvent(new DocumentUpdatedEvent(document));
    }

    public void remove(final Document document) {
        checkNotNull(document);

        if (Log.isDebugEnabled()) {
            Log.debug("Removing document: " + document);
        }

        // If the document is new remove it from the data-provider and emit an event (don't do anything remote)
        if (document.isNew()) {
            documentDataProvider.remove(document);
            fireDocumentRemoved(document);
            return;
        }

        final DocumentDTO data = ((DocumentImpl) document).get();
        waitWidget.startWaiting();

        documentRestService.removeDocument(data.getId(), new MethodCallback<String>()
        {
            public void onSuccess(final Method method, final String response) {
                Log.debug("Removed document: " + document);
                documentDataProvider.remove(document);
                fireDocumentRemoved(document);
                waitWidget.stopWaiting();
            }

            public void onFailure(final Method method, final Throwable exception) {
                serviceFailureNotifier.displayFailure(messages.documentRemoveFailed(), method, exception);
            }
        });
    }

    private void fireDocumentRemoved(final Document document) {
        eventBus.fireEvent(new DocumentRemovedEvent(document));
    }

    private class DocumentImpl
        implements Document
    {
        private DocumentDTO data;

        private boolean isNew;

        private boolean dirty;

        // TODO: When becoming dirty, save original and expose reset() to revert back to it

        private DocumentImpl(final DocumentDTO data) {
            this.data = checkNotNull(data);
        }

        private DocumentImpl() {
            this(new DocumentDTO().withId(uuid()));
            setNew(true);
        }

        public void setId(final String value) {
            data.setId(value);
            setDirty(true);
        }

        public String getId() {
            return data.getId();
        }

        public void setType(final DocumentTypeDTO value) {
            data.setType(value);
            setDirty(true);
        }

        public DocumentTypeDTO getType() {
            if (data.getType() == null) {
                return DocumentTypeDTO.SETTINGS;
            }
            return data.getType();
        }

        public void setName(final String value) {
            data.setName(value);
            setDirty(true);
        }

        public String getName() {
            return data.getName();
        }

        public void setDescription(final String value) {
            data.setDescription(value);
            setDirty(true);
        }

        public String getDescription() {
            return data.getDescription();
        }

        public void setContent(final String value) {
            data.setContent(value);
            setDirty(true);
        }

        public String getContent() {
            return data.getContent();
        }

        public List<DocumentAttributeDTO> getAttributes() {
            return data.getAttributes();
        }

        public DocumentDTO get() {
            return data;
        }

        public boolean isNew() {
            return isNew;
        }

        public void setNew(final boolean flag) {
            isNew = flag;

            // New implies dirty
            setDirty(flag);
        }

        public boolean isDirty() {
            return dirty;
        }

        public void setDirty(final boolean dirty) {
            this.dirty = dirty;
        }

        public ImageResource getIcon() {
            ImageResource icon;
            if (isNew()) {
                icon = icons.page_white_add();
            }
            else if (isDirty()) {
                icon = icons.page_white_edit();
            }
            else {
                icon = icons.page_white();
            }
            return icon;
        }

        public String getDisplayName() {
            if (getName() != null && getName().trim().length() != 0) {
                return getName() + " (" + getId() + ")";
            }
            else {
                return getId();
            }
        }

        public void updateAttributes(final DocumentDTO source) {
            checkNotNull(source);
            data.getAttributes().clear();
            data.getAttributes().addAll(source.getAttributes());
        }

        @Override
        public int hashCode() {
            return data.getId().hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != DocumentImpl.class) {
                return false;
            }

            DocumentImpl that = (DocumentImpl) obj;

            return that.get().getId().equals(data.getId());
        }

        @Override
        public String toString() {
            return "DocumentImpl{" +
                "id=" + data.getId() +
                ",type=" + data.getType() +
                ",name=" + data.getName() +
                ",new=" + isNew +
                ",dirty=" + dirty +
                ",attributes=" + data.getAttributes() +
                '}';
        }
    }
}
