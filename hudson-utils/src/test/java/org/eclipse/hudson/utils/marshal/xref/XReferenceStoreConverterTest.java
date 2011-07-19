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

package org.eclipse.hudson.utils.marshal.xref;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.eclipse.hudson.utils.marshal.XStreamMarshaller;
import org.eclipse.hudson.utils.marshal.xref.FileXReferenceStore;
import org.eclipse.hudson.utils.marshal.xref.XReference;
import org.eclipse.hudson.utils.marshal.xref.XReferenceStore;
import org.eclipse.hudson.utils.marshal.xref.XReferenceStoreConverter;
import org.hudsonci.utils.test.TestUtil;
import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests for {@link XReferenceStoreConverter}.
 */
public class XReferenceStoreConverterTest
{
    private final TestUtil util = new TestUtil(this);

    @Test
    public void testChewDocument() throws Exception {
        XStream xs = new XStream();
        xs.autodetectAnnotations(true);
        XStreamMarshaller marshaller = new XStreamMarshaller(xs);

        XReferenceStore store = new FileXReferenceStore(marshaller, util.resolveFile("target/test-xref"));
        XReferenceStoreConverter converter = new XReferenceStoreConverter(store, xs.getMapper(), xs.getReflectionProvider());
        xs.registerConverter(converter);

        Document<Record> doc1 = new Document<Record>();
        Record rec1 = new Record("1");
        Entity ent1 = new Entity("1");
        rec1.set(ent1);
        assertNotNull(rec1.entity.holder);
        assertTrue(rec1.entity.holder instanceof XReference.InstanceHolder);
        XReference.InstanceHolder holder1 = (XReference.InstanceHolder)rec1.entity.holder;
        assertNotNull(holder1.instance);

        doc1.records.add(rec1);

        StringWriter buff = new StringWriter();
        marshaller.marshal(doc1, buff);
        System.out.println("XML:\n" + buff);

        Document<Record> doc2 = (Document<Record>) marshaller.unmarshal(new StringReader(buff.toString()));
        assertNotNull(doc2);
        assertNotNull(doc2.records);
        assertEquals(1, doc2.records.size());

        Record rec2 = doc2.records.get(0);
        assertNotNull(rec2.entity);
        System.out.println("ENT REF: " + rec2.entity);
        assertNotNull(rec2.entity.holder);
        assertTrue(rec2.entity.holder instanceof XReferenceStoreConverter.UnmarshalHolder);

        XReferenceStoreConverter.UnmarshalHolder holder2 = (XReferenceStoreConverter.UnmarshalHolder)rec2.entity.holder;
        assertNull(holder2.instance);

        Entity ent2 = rec2.get();
        System.out.println("ENT: " + ent2);
        assertNotNull(ent2);
        assertNotNull(holder2.instance);

        buff = new StringWriter();
        marshaller.marshal(doc2, buff);
        System.out.println("XML\n" + buff);
    }

    @XStreamAlias("document")
    private static class Document<T>
    {
        public final List<T> records = new ArrayList<T>();
    }

    @XStreamAlias("record")
    private static class Record
    {
        private final String name;

        private XReference entity;

        private Record(String name) {
            this.name = name;
        }

        public void set(Entity value) {
            entity = new EntityReference(value);
        }

        public Entity get() {
            if (entity != null) {
                return (Entity) entity.get();
            }
            return null;
        }

        @XStreamAlias("entity-xref")
        private class EntityReference
            extends XReference
        {
            private EntityReference(final Entity value) {
                super(value);
            }

            @Override
            public String getPath() {
                return "test-" + name + ".xml";
            }
        }
    }

    @XStreamAlias("entity")
    private static class Entity
    {
        public final String name;

        private Entity(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Entity{" +
                "name='" + name + '\'' +
                '}';
        }
    }
}
