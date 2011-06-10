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

package org.hudsonci.utils.marshal.xref;

import org.hudsonci.utils.marshal.Marshaller;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reference to an externally serialized entity.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class XReference<T>
{
    protected transient Holder<T> holder;

    public XReference(final T value) {
        set(value);
    }

    public XReference() {
        // empty
    }

    public void set(final T value) {
        if (value != null) {
            holder = new InstanceHolder<T>(value);
        }
    }
    
    public T get() {
        if (holder != null) {
            return holder.get();
        }
        return null;
    }

    /**
     * Defines the path of the external reference.
     */
    public abstract String getPath();

    /**
     * Override to provide alternative marshalling.
     */
    public Marshaller getMarshaller() {
        return null;
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
            "holder=" + holder +
        '}';
    }

    /**
     * Provides delegation for instance access.
     */
    public static interface Holder<T>
    {
        T get();
    }

    /**
     * Holds on to a specific instance.
     */
    public static class InstanceHolder<T>
        implements Holder<T>
    {
        protected final T instance;

        protected InstanceHolder(final T instance) {
            this.instance = checkNotNull(instance);
        }

        public T get() {
            return instance;
        }

        @Override
        public String toString() {
            return "InstanceHolder{" +
                "instance=" + instance +
                '}';
        }
    }
}
