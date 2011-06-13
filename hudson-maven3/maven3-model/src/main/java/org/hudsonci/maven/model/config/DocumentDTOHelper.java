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

package org.hudsonci.maven.model.config;

import java.util.Iterator;

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
