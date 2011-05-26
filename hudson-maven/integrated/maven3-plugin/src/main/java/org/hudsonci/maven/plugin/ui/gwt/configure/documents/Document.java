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

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.view.client.ProvidesKey;
import org.hudsonci.maven.model.config.DocumentAttributeDTO;
import org.hudsonci.maven.model.config.DocumentTypeDTO;

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
