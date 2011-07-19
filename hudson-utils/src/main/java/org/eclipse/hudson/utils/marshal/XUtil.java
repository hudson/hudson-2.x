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

package org.eclipse.hudson.utils.marshal;

import org.eclipse.hudson.utils.id.OID;

import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XStream-related-ish utilities.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class XUtil
{
    private static final Logger log = LoggerFactory.getLogger(XUtil.class);

    private static final XStream xstream = new XStream();

    /**
     * Clones an object via {@link XStream}.
     *
     * This provides the ability to take a source object and convert it into a target object. Unless
     * there is some magic converter muck going on for in the source's marshaller this should be
     * sufficient to create target copies.
     */
    public static Object clone(final Object source) {
        if (source == null) {
            return null;
        }

        if (log.isTraceEnabled()) {
            log.trace("Cloning: {}", OID.get(source));
        }

        xstream.autodetectAnnotations(true);
        String xml = xstream.toXML(source);
        log.trace("Clone XML: {}", xml);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(source.getClass().getClassLoader());
        try {
            Object target = xstream.fromXML(xml);

            if (log.isTraceEnabled()) {
                log.trace("Clone: {}", OID.get(target));
            }

            return target;
        }
        finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    /**
     * Render the given object as XML.
     */
    public static String render(final Object source) {
        if (source == null) {
            return null;
        }

        xstream.autodetectAnnotations(true);
        String xml = xstream.toXML(source);
        log.trace("Rendered XML: {}", xml);

        return xml;
    }
}
