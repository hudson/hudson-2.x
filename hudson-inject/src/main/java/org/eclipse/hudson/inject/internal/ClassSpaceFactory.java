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

package org.eclipse.hudson.inject.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper to build {@link ClassSpace} instances.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
public class ClassSpaceFactory
{
    private static final Logger log = LoggerFactory.getLogger(ClassSpaceFactory.class);

    public ClassSpace create(final ClassLoader parent, final URL... urls) {
        return createClassSpace(parent, urls);
    }

    public ClassSpace create(final ClassLoader parent, final Class... types) {
        return createClassSpace(parent, scanTypes(types));
    }

    private URL[] scanTypes(final Class[] types) {
        assert types != null;
        List<URL> path = new ArrayList<URL>();
        for (Class type : types) {
            path.add(type.getProtectionDomain().getCodeSource().getLocation());
        }
        return path.toArray(new URL[path.size()]);
    }

    private ClassSpace createClassSpace(final ClassLoader parent, final URL[] urls) {
        if (log.isDebugEnabled()) {
            log.debug("Path:");
            for (URL url : urls) {
                log.debug("  {}", url);
            }
        }
        return new URLClassSpace(parent, urls);
    }
}
