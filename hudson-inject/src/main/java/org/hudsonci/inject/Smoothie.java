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

package org.hudsonci.inject;

import org.hudsonci.inject.internal.SmoothieContainerBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Smoothie container access.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public class Smoothie
{
    private static final Logger log = LoggerFactory.getLogger(Smoothie.class);

    private static SmoothieContainer container;

    /**
     * Set the container singleton.
     *
     * Can only be set once.
     *
     * @param instance  The container instance; must not be null
     */
    public static synchronized void setContainer(final SmoothieContainer instance) {
        checkState(container == null);
        container = checkNotNull(instance);
        log.debug("Container installed: {}", container);
    }

    /**
     * Get the container singleton.
     *
     * @return  The container instance; never null
     */
    public static synchronized SmoothieContainer getContainer() {
        if (container == null) {
            // This should really be done by a ServletContextListener when the webapp loads, but for now we are not modifying hudson-core, so bootstrap the container here.
            return new SmoothieContainerBootstrap().bootstrap();
        }
        return container;
    }
}
