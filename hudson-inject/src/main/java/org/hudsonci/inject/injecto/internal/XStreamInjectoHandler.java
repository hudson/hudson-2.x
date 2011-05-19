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

package org.hudsonci.inject.injecto.internal;

import org.hudsonci.inject.injecto.Injectomatic;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;
import hudson.XmlFile;
import hudson.model.Hudson;
import hudson.model.Items;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.model.UpdateCenter;
import hudson.util.RobustReflectionConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.EagerSingleton;

import javax.inject.Inject;
import javax.inject.Named;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles {@link Injectomatic} muck when unmarshalling components via XStream.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
@Named
@EagerSingleton
public class XStreamInjectoHandler
{
    private static final Logger log = LoggerFactory.getLogger(XStreamInjectoHandler.class);

    /**
     * Set of XStream instances which registration converters will be attached.
     */
    private static final XStream[] DEFAULT_XSTREAMS = {
        Hudson.XSTREAM,
        Items.XSTREAM,
        Queue.XSTREAM,
        Run.XSTREAM,
        UpdateCenter.XSTREAM,
        XmlFile.DEFAULT_XSTREAM
    };

    private final Injectomatic injecto;

    private final ReflectionProvider reflection;

    @Inject
    public XStreamInjectoHandler(final Injectomatic injecto, final ReflectionProvider reflection) {
        this.injecto = checkNotNull(injecto);
        this.reflection = checkNotNull(reflection);

        // Register converter for the default streams we know about
        for (XStream xs : DEFAULT_XSTREAMS) {
            register(xs);
        }
    }

    public void register(final XStream xs) {
        checkNotNull(xs);
        log.trace("Registering converter for: {}", xs);
        xs.registerConverter(new InjectoConverter(xs.getMapper(), reflection), XStream.PRIORITY_VERY_LOW);
    }

    /**
     * Handles injecto muck when unmarshalling.  Extends from {@link RobustReflectionConverter} to
     * make use of Hudson's old/unreadable data handling.
     */
    private class InjectoConverter
        extends RobustReflectionConverter
    {
        public InjectoConverter(final Mapper mapper, final ReflectionProvider provider) {
            super(mapper, provider);
        }

        public boolean canConvert(final Class type) {
            return injecto.isInjectable(type);
        }

        @Override
        public Object doUnmarshal(Object result, final HierarchicalStreamReader reader, final UnmarshallingContext context) {
            result = super.doUnmarshal(result, reader, context);
            injecto.inject(result);
            return result;
        }
    }
}
