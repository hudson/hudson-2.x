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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hudsonci.maven.model.PropertiesDTO.Entry;

/**
 * Helper for {@link PropertiesDTO}.
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class PropertiesDTOHelper
{
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

    public static void set(final PropertiesDTO source, final Object name, final Object value) {
        assert source != null;
        assert name != null;
        // value == null?

        String key = keyFor(name);

        Iterator<Entry> iter = source.getEntries().iterator();
        while (iter.hasNext()) {
            Entry entry = iter.next();
            if (key.equals(entry.getName())) {
                iter.remove();
                break;
            }
        }

        source.withEntries(new Entry().withName(key).withValue(String.valueOf(value)));
    }

    public static String get(final PropertiesDTO source, final Object name) {
        assert source != null;
        assert name != null;

        String key = keyFor(name);

        for (Entry entry : source.getEntries()) {
            if (key.equals(entry.getName())) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static boolean contains(final PropertiesDTO source, final Object name) {
        assert source != null;
        assert name != null;

        return get(source, name) != null;
    }

    public static <K, V> PropertiesDTO convert(final Map<K,V> source) {
        assert source != null;

        PropertiesDTO target = new PropertiesDTO();
        List<Entry> entries = target.getEntries();

        for (Map.Entry<K,V> entry : source.entrySet()) {
            entries.add(convert(entry));
        }

        return target;
    }

    public static <K, V> PropertiesDTO.Entry convert(final Map.Entry<K,V> source) {
        assert source != null;

        PropertiesDTO.Entry target = new PropertiesDTO.Entry()
            .withName(String.valueOf(source.getKey()))
            .withValue(String.valueOf(source.getValue()));

        return target;
    }
}
