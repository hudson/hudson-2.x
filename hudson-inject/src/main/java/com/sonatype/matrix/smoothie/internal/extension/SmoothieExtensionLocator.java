/**
 * The MIT License
 *
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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

package com.sonatype.matrix.smoothie.internal.extension;

import com.google.inject.Key;
import com.sonatype.matrix.smoothie.SmoothieContainer;
import hudson.ExtensionComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.locators.QualifiedBean;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Smoothie {@link ExtensionLocator}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.396
 */
@Named
@Singleton
public class SmoothieExtensionLocator
    implements ExtensionLocator
{
    private static final Logger log = LoggerFactory.getLogger(SmoothieExtensionLocator.class);

    private final SmoothieContainer container;

    @Inject
    public SmoothieExtensionLocator(final SmoothieContainer container) {
        this.container = checkNotNull(container);
    }

    /**
     * Look up extension type lists by asking the container for types with any {@link javax.inject.Qualifier} adorned annotation.
     */
    public <T> List<ExtensionComponent<T>> locate(final Class<T> type) {
        checkNotNull(type);

//        if (log.isTraceEnabled()) {
            log.info("Finding extensions: {}", type.getName());
//        }

        List<ExtensionComponent<T>> components = new ArrayList<ExtensionComponent<T>>();
        try {
            Iterable<QualifiedBean<Annotation,T>> items = container.locate(Key.get(type));
            for (QualifiedBean<Annotation,T> item : items) {
                // Use our container for extendability and logging simplicity.
                SmoothieComponent<T> component = new SmoothieComponent<T>(item);
                log.info("Found: {}", component);
                if (component.getInstance() != null) { // filter out null components (ie. uninitialized @Extension fields)
                    components.add(component);
                }
            }

//            if (log.isDebugEnabled()) {
                log.info("Found {} {} components", components.size(), type.getName());
//            }
        }
        catch (Exception e) {
            log.error("Extension discovery failed", e);
        }

        if (components.isEmpty()) {
            log.warn("No components of type '{}' discovered", type.getName());
        }

        return components;
    }
}
