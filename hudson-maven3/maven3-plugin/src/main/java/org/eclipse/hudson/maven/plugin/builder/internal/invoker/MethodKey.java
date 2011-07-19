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

package org.eclipse.hudson.maven.plugin.builder.internal.invoker;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Key for {@link Invoker} method lookup.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class MethodKey
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final int hash;

    public MethodKey(final String name, final Class[] types) {
        assert name != null;
        assert types != null;
        int result = name.hashCode();
        result = 31 * result + hashOf(types);
        this.hash = result;
    }

    public MethodKey(final Method method) {
        this(method.getName(), method.getParameterTypes());
    }

    private static int hashOf(final Class[] types) {
        if (types == null) {
            return 0;
        }

        int result = 1;
        for (Class type : types) {
            result = 31 * result + (type == null ? 0 : type.getName().hashCode());
        }

        return result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        MethodKey that = (MethodKey) obj;

        return hash == that.hash;
    }

    @Override
    public String toString() {
        return "MethodKey{" +
                "hash=" + hash +
                '}';
    }
}
