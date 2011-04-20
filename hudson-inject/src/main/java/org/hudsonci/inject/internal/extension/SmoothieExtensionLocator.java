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

package org.hudsonci.inject.internal.extension;

import com.google.inject.Key;

import org.hudsonci.inject.SmoothieContainer;

import hudson.Extension;
import hudson.ExtensionComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.BeanEntry;

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
 * @since 1.397
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

        if (log.isDebugEnabled()) {
            log.debug("Finding extensions: {}", type.getName());
        }

        List<ExtensionComponent<T>> components = new ArrayList<ExtensionComponent<T>>();
        for (BeanEntry<Annotation,T> item : container.locate(Key.get(type))) {
            try {
                // Use our container for extendability and logging simplicity.
                SmoothieComponent<T> component = new SmoothieComponent<T>(item);
                log.debug("Found: {}", component);
                if (component.getInstance() != null) { // filter out null components (ie. uninitialized @Extension fields)
                    components.add(component);
                }
            } catch (Throwable e) {
                if (SmoothieComponent.isOptional(item)) {
                    log.debug("Failed to create optional extension", e);
                } else {
                    log.warn("Failed to create extension", e);                    
                }
            }
        }

        if (log.isDebugEnabled()) {
            if (components.isEmpty()) {
                log.debug("No components of type '{}' discovered", type.getName());
            } else {
                log.debug("Found {} {} components", components.size(), type.getName());
            }
        }

        return components;
    }
}
