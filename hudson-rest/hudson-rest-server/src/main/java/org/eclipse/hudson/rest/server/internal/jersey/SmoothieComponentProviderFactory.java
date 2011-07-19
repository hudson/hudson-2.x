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

package org.eclipse.hudson.rest.server.internal.jersey;

import com.google.inject.Key;

import org.eclipse.hudson.inject.SmoothieContainer;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.BeanEntry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides IoC support for loading Jersey components via {@link SmoothieContainer}.
 *
 * @author <a href="mailto:jjfarcand@apache.org">Jeanfrancois Arcand</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named("smoothie")
@Singleton
public class SmoothieComponentProviderFactory
    implements IoCComponentProviderFactory
{
    private static final Logger log = LoggerFactory.getLogger(SmoothieComponentProviderFactory.class);

    // TODO: We can actually use the SisuComponentProviderFactory here, as SmoothieContainer is a thin layer over the BeanLocator.
    // TODO: ... just need SisuComponentProviderFactory in a common place, atm hudson-rest-common does not have any Jersey deps

    private final SmoothieContainer container;

    @Inject
    public SmoothieComponentProviderFactory(final SmoothieContainer container) {
        this.container = checkNotNull(container);
    }

    public IoCComponentProvider getComponentProvider(final Class<?> type) {
        checkNotNull(type);
        log.trace("Get component provider: {}", type);

        @SuppressWarnings({"unchecked"}) // dropping <?> from type, it can cause compile errors due to incompatible types from capture# :-(
        Iterator<BeanEntry<Annotation, ?>> iter = container.locate(Key.get((Class)type)).iterator();
        if (iter.hasNext()) {
            final BeanEntry entry = iter.next();
            log.trace("Found component: {}", entry);

            return new IoCInstantiatedComponentProvider()
            {
                public Object getInjectableInstance(final Object obj) {
                    return obj;
                }

                public Object getInstance() {
                    return entry.getValue();
                }
            };
        }

        return null;
    }

    public IoCComponentProvider getComponentProvider(final ComponentContext context, final Class<?> type) {
        return getComponentProvider(type);
    }
}
