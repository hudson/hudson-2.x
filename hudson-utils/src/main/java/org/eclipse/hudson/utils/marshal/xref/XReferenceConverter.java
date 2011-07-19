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

import org.eclipse.hudson.utils.marshal.xref.XReference.InstanceHolder;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import java.io.IOException;
import java.lang.ref.SoftReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converter for {@link XReference} types.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class XReferenceConverter
    extends AbstractReflectionConverter
{
    protected HolderType holderType = HolderType.HARD;

    public XReferenceConverter(final Mapper mapper, final ReflectionProvider reflection) {
        super(mapper, reflection);
    }

    public HolderType getHolderType() {
        return holderType;
    }

    public void setHolderType(final HolderType type) {
        this.holderType = checkNotNull(type);
    }

    public boolean canConvert(final Class type) {
        return XReference.class.isAssignableFrom(type);
    }

    @Override
    protected void doMarshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
        // Do the default marshalling for the reference container
        super.doMarshal(source, writer, context);

        // Then delegate the storage of the reference target
        XReference ref = (XReference)source;
        Object target = ref.get();
        if (target != null) {
            try {
                store(ref);
                ref.holder = createStoredHolder(ref, target);
            }
            catch (Exception e) {
                throw new ConversionException("Failed to marshal reference: " + ref, e);
            }
        }
    }

    @Override
    public Object doUnmarshal(final Object result, final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        // Do the default unmarshalling for the reference container
        XReference ref = (XReference) super.doUnmarshal(result, reader, context);
        ref.holder = createUnmarshalHolder(ref);
        return ref;
    }

    /**
     * Provides reference storing behavior.
     */
    protected abstract void store(final XReference ref) throws IOException;

    /**
     * Provides reference loading behavior.
     */
    protected abstract Object load(final XReference ref) throws IOException;

    /**
     * Create the holder to be used after the reference has been stored.
     */
    @SuppressWarnings({"unchecked"})
    protected XReference.Holder createStoredHolder(final XReference ref, final Object target) {
        switch (holderType) {
            case HARD:
                return new InstanceHolder(target);
            case SOFT:
                return new SoftUnmarshalHolder(ref, target);
            default:
                throw new Error();
        }
    }

    /**
     * Create the holder to be used after the reference has been unmarshalled.
     */
    protected XReference.Holder createUnmarshalHolder(final XReference ref) {
        switch (holderType) {
            case HARD:
                return new UnmarshalHolder(ref);
            case SOFT:
                return new SoftUnmarshalHolder(ref);
            default:
                throw new Error();
        }
    }

    /**
     * Support for {@link XReference.Holder} implementations.
     */
    protected abstract class HolderSupport
        implements XReference.Holder
    {
        protected final XReference ref;

        protected HolderSupport(final XReference ref) {
            this.ref = checkNotNull(ref);
        }

        protected Object doLoad() {
            try {
                return load(ref);
            }
            catch (Exception e) {
                throw new ConversionException("Failed to unmarshal reference: " + ref, e);
            }
        }
    }

    /**
     * Default holder types.
     */
    public static enum HolderType
    {
        /**
         * Use hard references.
         */
        HARD,

        /**
         * Use soft references.
         */
        SOFT
    }

    /**
     * Unmarshalling holder with a hard reference.
     */
    protected class UnmarshalHolder
        extends HolderSupport
    {
        protected Object instance;

        protected UnmarshalHolder(final XReference ref) {
            super(ref);
        }

        public Object get() {
            if (instance == null) {
                instance = doLoad();
            }
            return instance;
        }

        @Override
        public String toString() {
            return "UnmarshalHolder{" +
                "instance=" + instance +
                '}';
        }
    }

    /**
     * Unmarshalling holder with a soft reference.
     */
    @SuppressWarnings({"unchecked"})
    protected class SoftUnmarshalHolder
        extends HolderSupport
    {
        protected SoftReference instance;

        protected SoftUnmarshalHolder(final XReference ref) {
            super(ref);
        }

        protected SoftUnmarshalHolder(final XReference ref, final Object target) {
            super(ref);
            checkNotNull(target);
            this.instance = new SoftReference(target);
        }

        public Object get() {
            Object target;
            if (instance == null || (target = instance.get()) == null) {
                target = doLoad();
                instance = new SoftReference(target);
            }
            return target;
        }

        @Override
        public String toString() {
            return "SoftUnmarshalHolder{" +
                "instance=" + instance +
                '}';
        }
    }
}
