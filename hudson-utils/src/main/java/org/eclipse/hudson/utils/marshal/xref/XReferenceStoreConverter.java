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

import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.mapper.Mapper;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converter for {@link XReference} types using {@link XReferenceStore} strategy.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class XReferenceStoreConverter
    extends XReferenceConverter
{
    private final XReferenceStore store;

    public XReferenceStoreConverter(final XReferenceStore store, final Mapper mapper, final ReflectionProvider reflection) {
        super(mapper, reflection);
        this.store = checkNotNull(store);
    }

    @Override
    protected void store(final XReference ref) throws IOException {
        assert ref != null;
        store.store(ref);
    }

    @Override
    protected Object load(final XReference ref) throws IOException {
        assert ref != null;
        return store.load(ref);
    }
}
