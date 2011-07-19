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

import com.thoughtworks.xstream.XStream;

import java.io.Reader;
import java.io.Writer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * XStream {@link Marshaller}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class XStreamMarshaller
    implements Marshaller
{
    private final XStream xstream;

    public XStreamMarshaller(final XStream xstream) {
        this.xstream = checkNotNull(xstream);
    }

    public void marshal(final Object object, final Writer writer) {
        xstream.toXML(object, writer);
    }

    public Object unmarshal(final Reader reader) {
        return xstream.fromXML(reader);
    }
}
