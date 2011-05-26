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

package org.hudsonci.rest.client.internal.jersey;

import com.google.inject.Key;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.inject.BeanEntry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides IoC support for loading Jersey components via Sisu's {@link BeanLocator}.
 *
 * @author <a href="mailto:jjfarcand@apache.org">Jeanfrancois Arcand</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named("sisu")
@Singleton
public class SisuComponentProviderFactory
    implements IoCComponentProviderFactory
{
    private static final Logger log = LoggerFactory.getLogger(SisuComponentProviderFactory.class);

    private final BeanLocator container;

    @Inject
    public SisuComponentProviderFactory(final BeanLocator container) {
        this.container = checkNotNull(container);
        log.debug("Container: {}", container);
    }

    public IoCComponentProvider getComponentProvider(final Class<?> type) {
        checkNotNull(type);

        // FIXME: Flip to trace once we confirm this is working
        log.debug("Get component provider: {}", type);

        @SuppressWarnings({"unchecked"}) // dropping <?> from type, it can cause compile errors due to incompatible types from capture# :-(
        Iterator<BeanEntry<Annotation, ?>> iter = container.locate(Key.get((Class)type)).iterator();
        if (iter.hasNext()) {
            final BeanEntry entry = iter.next();
            log.debug("Found component: {}", entry); // FIXME: same here

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
