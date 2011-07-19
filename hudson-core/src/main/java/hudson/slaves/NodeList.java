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

package hudson.slaves;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import hudson.model.Node;
import hudson.util.RobustCollectionConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@link CopyOnWriteArrayList} for {@link Node} that has special serialization semantics
 * of not serializing {@link EphemeralNode}s.
 *
 * @author Kohsuke Kawaguchi
 */
public final class NodeList extends CopyOnWriteArrayList<Node> {
    public NodeList() {
    }

    public NodeList(Collection<? extends Node> c) {
        super(c);
    }

    public NodeList(Node[] toCopyIn) {
        super(toCopyIn);
    }

    /**
     * {@link Converter} implementation for XStream.
     *
     * Serializaion form is compatible with plain {@link List}.
     */
    public static final class ConverterImpl extends RobustCollectionConverter {
        public ConverterImpl(XStream xstream) {
            super(xstream);
        }

        @Override
        public boolean canConvert(Class type) {
            return type==NodeList.class;
        }

        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            for (Node o : (NodeList) source) {
                if(o instanceof EphemeralNode)
                    continue;   // skip
                writeItem(o, context, writer);
            }
        }

        @Override
        protected Object createCollection(Class type) {
            return new ArrayList();
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            return new NodeList((List<Node>)super.unmarshal(reader, context));
        }
    }
}
