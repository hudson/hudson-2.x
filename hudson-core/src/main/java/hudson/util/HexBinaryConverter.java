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

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import hudson.Util;

/**
 * @author Kohsuke Kawaguchi
 */
public class HexBinaryConverter implements Converter {

    public boolean canConvert(Class type) {
        return type==byte[].class;
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        byte[] data = (byte[]) source;
        writer.setValue(Util.toHexString(data));
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String data = reader.getValue(); // needs to be called before hasMoreChildren.
        return Util.fromHexString(data);
    }
}
