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

package org.hudsonci.maven.plugin.ui.gwt.configure.documents;

import org.hudsonci.maven.plugin.ui.gwt.configure.documents.internal.DocumentDetailViewImpl;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.HasData;
import com.google.inject.ImplementedBy;
import org.hudsonci.maven.model.config.DocumentAttributeDTO;
import org.hudsonci.maven.model.config.DocumentTypeDTO;

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
