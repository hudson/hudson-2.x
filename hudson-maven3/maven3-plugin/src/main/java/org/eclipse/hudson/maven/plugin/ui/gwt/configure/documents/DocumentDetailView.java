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

import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.internal.DocumentDetailViewImpl;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.HasData;
import com.google.inject.ImplementedBy;
import org.eclipse.hudson.maven.model.config.DocumentAttributeDTO;
import org.eclipse.hudson.maven.model.config.DocumentTypeDTO;

/**
 * Provides the UI for editing a document.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@ImplementedBy(DocumentDetailViewImpl.class)
public interface DocumentDetailView
    extends IsWidget
{
    @DefaultLocale("en_US")
    interface MessagesResource
        extends Messages
    {
        @DefaultMessage("ID")
        String id();

        @DefaultMessage("Type")
        String type();

        @DefaultMessage("Name")
        String name();

        @DefaultMessage("Description")
        String description();

        @DefaultMessage("Attributes")
        String attributes();

        @DefaultMessage("Save")
        String save();

        @DefaultMessage("Update")
        String update();

        @DefaultMessage("Cancel")
        String cancel();

        @DefaultMessage("Revert")
        String revert();
    }

    void setPresenter(DocumentDetailPresenter presenter);

    void setId(String value);

    String getId();

    void setType(DocumentTypeDTO value);

    DocumentTypeDTO getType();

    void setName(String value);

    String getName();

    void setDescription(String value);

    String getDescription();

    HasData<DocumentAttributeDTO> getAttributesDataContainer();

    void setContent(String value);

    String getContent();

    void setNewDocument(boolean flag);

    void clear();
}
