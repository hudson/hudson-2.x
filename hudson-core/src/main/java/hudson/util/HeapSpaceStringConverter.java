/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.util;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.converters.basic.StringConverter;

/**
 * The default {@link StringConverter} in XStream
 * uses {@link String#intern()}, which stresses the
 * (rather limited) PermGen space with a large XML file.
 *
 * <p>
 * Use this to avoid that (instead those strings will
 * now be allocated to the heap space.)
 *
 * @author Kohsuke Kawaguchi
 */
public class HeapSpaceStringConverter extends AbstractSingleValueConverter {

    public boolean canConvert(Class type) {
        return type.equals(String.class);
    }

    public Object fromString(String str) {
        return str;
    }
}
