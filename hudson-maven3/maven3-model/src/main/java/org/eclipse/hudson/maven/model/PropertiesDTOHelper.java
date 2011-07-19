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

package org.eclipse.hudson.maven.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.hudson.maven.model.PropertiesDTO;
import org.eclipse.hudson.maven.model.PropertiesDTO.Entry;

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
