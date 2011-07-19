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

package org.eclipse.hudson.maven.model.config;

import java.util.Iterator;

import org.eclipse.hudson.maven.model.config.DocumentAttributeDTO;
import org.eclipse.hudson.maven.model.config.DocumentDTO;

/**
 * Helper for {@link DocumentDTO}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class DocumentDTOHelper
{
    public static String asString(final DocumentDTO source) {
        assert source != null;

        StringBuilder buff = new StringBuilder();
        buff.append("{id=").append(source.getId())
            .append(",name=").append(source.getName())
            .append(",type=").append(source.getType());
        if (source.getDescription() != null) {
            buff.append(",description=").append(source.getDescription());
        }
        if (source.getContent() != null) {
            buff.append(",content-size=").append(source.getContent().length());
        }
        if (source.getAttributes() != null) {
            buff.append(",attributes=").append(source.getAttributes());
        }
        buff.append("}");

        return buff.toString();
    }

    private static String keyFor(final Object name) {
        String key;
        if (name instanceof Enum) {
            key = ((Enum)name).name();
        }
        else {
            key = String.valueOf(name);
        }
        return key;
    }

    public static void setAttribute(final DocumentDTO source, final Object name, final Object value) {
        assert source != null;
        assert name != null;
        // value == null?

        String key = keyFor(name);

        // Find existing attribute and remove it
        Iterator<DocumentAttributeDTO> iter = source.getAttributes().iterator();
        while (iter.hasNext()) {
            DocumentAttributeDTO attr = iter.next();
            if (key.equals(attr.getName())) {
                iter.remove();
                break;
            }
        }

        // Add new attribute at the end
        source.withAttributes(new DocumentAttributeDTO().withName(key).withValue(String.valueOf(value)));
    }

    public static Object getAttribute(final DocumentDTO source, final Object name) {
        assert source != null;
        assert name != null;

        String key = keyFor(name);

        // Find existing attribute and remove it
        for (DocumentAttributeDTO attr : source.getAttributes()) {
            if (key.equals(attr.getName())) {
                return attr.getValue();
            }
        }

        return null;
    }
}
