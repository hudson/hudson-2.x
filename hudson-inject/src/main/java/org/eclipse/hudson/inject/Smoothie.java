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

package org.eclipse.hudson.inject;

import org.eclipse.hudson.inject.internal.SmoothieContainerBootstrap;
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
