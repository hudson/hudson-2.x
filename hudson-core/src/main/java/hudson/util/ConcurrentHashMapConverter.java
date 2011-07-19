/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.SerializableConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ConcurrentHashMap} should convert like a map, instead of via serialization.
 *
 * @author Kohsuke Kawaguchi
 */
public class ConcurrentHashMapConverter extends MapConverter {
    private final SerializableConverter sc;

    public ConcurrentHashMapConverter(XStream xs) {
        this(xs.getMapper(),xs.getReflectionProvider());
    }

    public ConcurrentHashMapConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
        super(mapper);
        sc = new SerializableConverter(mapper,reflectionProvider);
    }

    @Override
    public boolean canConvert(Class type) {
        return type==ConcurrentHashMap.class;
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        // ConcurrentHashMap used to serialize as custom serialization,
        // so read it in a compatible fashion.
        String s = reader.getAttribute("serialization");
        if(s!=null && s.equals("custom")) {
            return sc.unmarshal(reader,context);
        } else {
            return super.unmarshal(reader, context);
        }
    }
}
