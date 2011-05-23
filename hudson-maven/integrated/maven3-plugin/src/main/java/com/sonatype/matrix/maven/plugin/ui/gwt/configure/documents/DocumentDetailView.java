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
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.HasData;
import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.model.config.DocumentAttributeDTO;
import com.sonatype.matrix.maven.model.config.DocumentTypeDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.internal.DocumentDetailViewImpl;

/**
 * Provides the UI for editing a document.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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
