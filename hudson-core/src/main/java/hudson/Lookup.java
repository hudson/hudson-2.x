/*******************************************************************************
 *
 * Copyright (c) 2010, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *       Kohsuke Kawaguchi
 *
 *******************************************************************************/ 

package hudson;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Type-safe instance map.
 *
 * @author Kohsuke Kawaguchi
 */
public class Lookup {
    private final ConcurrentHashMap<Class,Object> data = new ConcurrentHashMap<Class,Object>();

    public <T> T get(Class<T> type) {
        return type.cast(data.get(type));
    }

    public <T> T set(Class<T> type, T instance) {
        return type.cast(data.put(type,instance));
    }

    /**
     * Overwrites the value only if the current value is null.
     *
     * @return
     *      If the value was null, return the {@code instance} value.
     *      Otherwise return the current value, which is non-null.
     */
    public <T> T setIfNull(Class<T> type, T instance) {
        Object o = data.putIfAbsent(type, instance);
        if (o!=null)    return type.cast(o);
        return instance;
    }
}
