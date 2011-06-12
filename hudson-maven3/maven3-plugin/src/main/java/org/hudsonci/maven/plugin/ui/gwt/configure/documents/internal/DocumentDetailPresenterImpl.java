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

package org.hudsonci.maven.plugin.ui.gwt.configure.documents.internal;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import org.hudsonci.maven.model.config.DocumentAttributeDTO;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hudsonci.maven.plugin.ui.gwt.configure.documents.Document;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.DocumentDetailPresenter;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.DocumentDetailView;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.DocumentManagerLiaison;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.event.DocumentAddedEvent;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.event.DocumentSelectedEvent;
import org.hudsonci.maven.plugin.ui.gwt.configure.documents.event.DocumentUpdatedEvent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link DocumentDetailPresenter}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Singleton
public class DocumentDetailPresenterImpl
    implements DocumentDetailPresenter
{
    private final DocumentDetailView view;

    private final DocumentManagerLiaison documentManagerLiaison;

    private final ListDataProvider<DocumentAttributeDTO> attributesDataProvider;

    private Document current;

    @Inject
    public DocumentDetailPresenterImpl(final DocumentDetailView view, final DocumentManagerLiaison documentManagerLiaison) {
        this.view = checkNotNull(view);
        this.documentManagerLiaison = checkNotNull(documentManagerLiaison);

        view.setPresenter(this);

        // Hook up the data provider for attributes
        attributesDataProvider = new ListDataProvider<DocumentAttributeDTO>();
        view.getAttributesDataContainer().setSelectionModel(new NoSelectionModel<DocumentAttributeDTO>());
        attributesDataProvider.addDataDisplay(view.getAttributesDataContainer());
    }

    public DocumentDetailView getView() {
        return view;
    }

    public void setDocument(final Document document) {
        current = document;
        if (document != null) {
            // Fill up the form with the document details
            view.setId(document.getId());
            view.setType(document.getType());
            view.setName(document.getName());
            view.setDescription(document.getDescription());
            view.setContent(document.getContent());
            view.setNewDocument(document.isNew());
            attributesDataProvider.setList(document.getAttributes());

            // Make the view visible
            view.asWidget().setVisible(true);
        }
        else {
            // Clear the form and hide the view
            view.asWidget().setVisible(false);
        }
    }

    public void onDocumentSelected(final DocumentSelectedEvent event) {
        checkNotNull(event);
        Document document = event.getDocument();
        Log.debug("Document selected: " + document);
        setDocument(document);
    }

    public void onDocumentUpdated(final DocumentUpdatedEvent event) {
        checkNotNull(event);
        Document document = event.getDocument();
        Log.debug("Document updated: " + document);
        setDocument(document);
    }

    public void onDocumentAdded(final DocumentAddedEvent event) {
        checkNotNull(event);
        Document document = event.getDocument();
        Log.debug("Document added: " + document);
        setDocument(document);
    }

    public void doSave() {
        checkNotNull(current);

        // FIXME: Should not allow a record w/o a "name" to be saved.
        // FIXME: Need to implement some sort of form validation

        // Copy the view details into the current document
        current.setId(view.getId());
        current.setType(view.getType());
        current.setName(view.getName());
        current.setDescription(view.getDescription());
        current.setContent(view.getContent());

        // Save the document
        documentManagerLiaison.save(current);
    }

    public void doCancel() {
        if (current == null) {
            return;
        }

        if (current.isNew()) {
            // clear temporal document and hide the view
            documentManagerLiaison.remove(current);
            view.clear();
            view.asWidget().setVisible(false);
            attributesDataProvider.getList().clear();
            current = null;
        }
        else {
            // reset the view to the current data
            setDocument(current);
        }
    }
}
