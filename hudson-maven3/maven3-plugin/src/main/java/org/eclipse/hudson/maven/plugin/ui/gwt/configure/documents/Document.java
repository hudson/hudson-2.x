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

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.view.client.ProvidesKey;
import org.eclipse.hudson.maven.model.config.DocumentAttributeDTO;
import org.eclipse.hudson.maven.model.config.DocumentTypeDTO;

import java.util.List;

/**
 * Abstraction of a Maven configuration document.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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
        public Object getKey(final Document document) {
            return document != null ? document.getId() : null;
        }
    };
}
