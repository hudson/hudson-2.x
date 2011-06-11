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

package org.hudsonci.rest.common;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class JacksonJaxbAnnotationsTest
{
    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Order is important here, Jackson needs to be first, then Jaxb so that @JsonProperty gets picked up.
        AnnotationIntrospector pair = new AnnotationIntrospector.Pair(
            new JacksonAnnotationIntrospector(),
            new JaxbAnnotationIntrospector()
        );
        mapper.getDeserializationConfig().setAnnotationIntrospector(pair);
        mapper.getSerializationConfig().setAnnotationIntrospector(pair);
        this.mapper = mapper;
    }

    @Test
    public void testProperties() throws Exception {
        PropertiesDTO props = new PropertiesDTO();

        PropertiesDTO.Entry entry;
        entry = new PropertiesDTO.Entry();
        entry.setName("foo");
        entry.setValue("bar");
        props.getEntries().add(entry);

        entry = new PropertiesDTO.Entry();
        entry.setName("a");
        entry.setValue("b");
        props.getEntries().add(entry);

        String value = mapper.writeValueAsString(props);
        System.out.println(value);

        assertEquals("{\"entries\":[{\"name\":\"foo\",\"value\":\"bar\"},{\"name\":\"a\",\"value\":\"b\"}]}", value);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "properties", propOrder = {
        "entries"
    })
    @XmlRootElement(name="properties")
    public static class PropertiesDTO
    {
        @JsonProperty("entries")
        @XmlElement(name = "entry", required = true)
        protected List<PropertiesDTO.Entry> entries;

        public List<PropertiesDTO.Entry> getEntries() {
            if (entries == null) {
                entries = new ArrayList<PropertiesDTO.Entry>();
            }
            return this.entries;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "name",
            "value"
        })
        public static class Entry
        {
            @XmlElement(required = true)
            protected String name;

            @XmlElement(required = true)
            protected String value;

            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}
