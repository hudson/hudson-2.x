/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.view.client.ProvidesKey;
import com.sonatype.matrix.maven.model.config.DocumentAttributeDTO;
import com.sonatype.matrix.maven.model.config.DocumentTypeDTO;

import java.util.List;

/**
 * Abstraction of a Maven configuration document.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public interface Document
{
    void setId(String value);

    String getId();

    void setType(DocumentTypeDTO value);

    DocumentTypeDTO getType();

    void setName(String value);

    String getName();

    void setDescription(String value);

    String getDescription();

    void setContent(String value);

    String getContent();

    List<DocumentAttributeDTO> getAttributes();

    ImageResource getIcon();

    String getDisplayName();

    // TODO: Hold on to original, expose reset()

    boolean isNew();

    boolean isDirty();

    ProvidesKey<Document> KEY_PROVIDER = new ProvidesKey<Document>()
    {
        @Override
        public Object getKey(final Document document) {
            return document != null ? document.getId() : null;
        }
    };
}
