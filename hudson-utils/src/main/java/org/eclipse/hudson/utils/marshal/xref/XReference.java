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

package org.eclipse.hudson.utils.marshal.xref;

import org.eclipse.hudson.utils.marshal.Marshaller;

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
