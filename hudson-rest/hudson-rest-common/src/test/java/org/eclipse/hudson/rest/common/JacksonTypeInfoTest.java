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

package org.eclipse.hudson.rest.common;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.eclipse.hudson.rest.common.JacksonCodec;
import org.eclipse.hudson.rest.common.JsonCodec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for Jackson's {@link JsonTypeInfo} annotation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class JacksonTypeInfoTest
{
    private JsonCodec codec;

    @Before
    public void setUp() throws Exception {
        codec = new JacksonCodec();
    }
    @Test
    public void testTypeInfo() throws Exception {
        Items items = new Items();
        items.children = new ArrayList<Data>();
        items.children.add(new A());
        items.children.add(new B());
        items.children.add(new C());

        String value = codec.encode(items);
        System.out.println("VALUE: " + value);

        Items items2 = codec.decode(value, Items.class);
        assertNotNull(items2.children);
        assertEquals(3, items2.children.size());
        assertEquals(A.class, items2.children.get(0).getClass());
        assertEquals("a", items2.children.get(0).getData());
        assertEquals(B.class, items2.children.get(1).getClass());
        assertEquals("b", items2.children.get(1).getData());
    }

    @JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
    public interface Data
    {
        String getData();
    }

    public static abstract class Base
        implements Data
    {
        // empty
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "a")
    public static class A
        extends Base
    {
        @XmlElement(name = "data")
        private String data = "a";

        public String getData() {
            return data;
        }
        
        public void setData(String data) {
            this.data = data;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "b")
    public static class B
        extends Base
    {
        @XmlElement(name = "data")
        private String data = "b";

        public String getData() {
            return data;
        }
        
        public void setData(String data) {
            this.data = data;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "c")
    public static class C
        implements Data
    {
        @XmlElement(name = "data")
        @JsonProperty("data")
        private String data = "c";

        public String getData() {
            return data;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "items")
    public static class Items
    {
        @XmlElement(name = "children")
        @JsonProperty("children")
        private List<Data> children;
    }
}
