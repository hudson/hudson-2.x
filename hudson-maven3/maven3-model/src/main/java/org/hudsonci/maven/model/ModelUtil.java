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

package org.hudsonci.maven.model;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Various model utilities.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ModelUtil
{
    public static boolean isEmpty(final String value) {
        return value == null || value.trim().length() == 0;
    }

    public static boolean isSet(final Boolean value) {
        return value != null && value;
    }

    public static String renderProperties(final PropertiesDTO source) {
        // source could be null
        if (source == null || source.getEntries().isEmpty()) {
            return "";
        }

        StringBuilder buff = new StringBuilder();

        for (PropertiesDTO.Entry entry : source.getEntries()) {
            buff.append(entry.getName()).append("=").append(entry.getValue()).append("\n");
        }

        return buff.toString();
    }

    // FIXME: Need to handle cases where -Da=b and a=b are mixed.

    public static PropertiesDTO parseProperties(final String source) {
        // source could be null;
        if (source == null) {
            return null;
        }

        if (source.trim().startsWith("-D")) {
            return parsePropertyOptions(source);
        }

        Properties props = new Properties();
        try {
            // NOTE: Using SBIS here for java5 compat
            props.load(new StringBufferInputStream(source));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return PropertiesDTOHelper.convert(props);
    }

    public static PropertiesDTO parsePropertyOptions(final String source) {
        // source could be null;
        if (source == null) {
            return null;
        }

        PropertiesDTO props = new PropertiesDTO();
        String[] items = source.trim().split("-D");
        for (String item : items) {
            if (item.length() != 0) {
                NameValue nv = NameValue.parse(item);
                props.getEntries().add(new PropertiesDTO.Entry().withName(nv.name).withValue(nv.value));
            }
        }

        return props;
    }

    public static String renderList(final List<String> source) {
        // source could be null
        if (source == null || source.isEmpty()) {
            return "";
        }

        StringBuilder buff = new StringBuilder();

        for (String item : source) {
            buff.append(item).append("\n");
        }

        return buff.toString();
    }

    public static List<String> parseList(final String source) {
        // source could be null;
        if (source == null || source.trim().length() == 0) {
            return null;
        }

        String[] items = source.trim().split("[\\s,]");
        List<String> target = new ArrayList<String>(items.length);

        for (String item : items) {
            if (item.length() != 0) {
                target.add(item);
            }
        }

        return target;
    }
}
