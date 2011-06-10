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

package org.hudsonci.utils.marshal;

import org.hudsonci.utils.id.OID;
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
