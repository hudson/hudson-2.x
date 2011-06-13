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

package org.hudsonci.maven.plugin.builder.internal.invoker;

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
