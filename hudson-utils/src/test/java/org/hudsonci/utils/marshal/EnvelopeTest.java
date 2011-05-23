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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.core.JVM;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ObjectStreamClass;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link Envelope}.
 */
public class EnvelopeTest
{
    private XStream xs;

    @Before
    public void setUp() throws Exception {
        xs = new XStream();
        xs.registerConverter(new EnvelopeConverter(xs.getMapper(), new JVM().bestReflectionProvider()));
        xs.autodetectAnnotations(true);
    }

    @After
    public void tearDown() throws Exception {
        xs = null;
    }

    private void chew(final Data data) {
        System.out.println("Chewing: " + data);

        Envelope env1 = new Envelope<Data>(data);
        String xml = xs.toXML(env1);
        System.out.println("XML:\n" + xml);

        @SuppressWarnings({"unchecked"})
        Envelope<Data> env2 = (Envelope<Data>) xs.fromXML(xml);
        System.out.println("ENV: " + env2);

        assertEquals(env1.getVersion(), env2.getVersion());
        assertEquals(env1.getSerial(), env2.getSerial());
        assertEquals(data.getClass(), env2.getContent().getClass());
        assertEquals(data.name, env2.getContent().name);

        System.out.println();
    }

    @Test
    public void testNonSerializableData() throws Exception {
        chew(new NonSerializableData("a"));
    }

    @Test
    public void testSerializableData() throws Exception {
        chew(new SerializableData("b"));
    }

    @Test
    public void testSerializableData2() throws Exception {
        chew(new SerializableData2("c"));
    }

    @Test
    public void testAnnotationData() throws Exception {
        chew(new AnnotationData("d"));
    }

    @Test
    public void testAnnotationData2() throws Exception {
        chew(new AnnotationData2("d"));
    }

    @Test
    public void testSerialVersionHelper() {
        Long value = SerialVersionHelper.get(new SerializableData("foo"));
        assertNotNull(value);
        assertEquals(ObjectStreamClass.lookup(SerializableData.class).getSerialVersionUID(), (long)value);

        value = SerialVersionHelper.get(new SerializableData2("foo"));
        assertNotNull(value);
        assertEquals(99, (long)value);

        value = SerialVersionHelper.get(new NonSerializableData("foo"));
        assertNotNull(value);
        assertEquals(0, (long)value);

        value = SerialVersionHelper.get(new AnnotationData("foo"));
        assertNotNull(value);
        assertEquals(1, (long)value);

        value = SerialVersionHelper.get(new AnnotationData2("foo"));
        assertNotNull(value);
        assertEquals(2, (long)value);
    }

    private static class Data
    {
        @XStreamAsAttribute
        public final String name;

        public Data(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    @XStreamAlias("non-serializable-data")
    private static class NonSerializableData
        extends Data
    {
        public NonSerializableData(String name) {
            super(name);
        }
    }

    @XStreamAlias("serializable-data")
    private static class SerializableData
        extends Data
        implements Serializable
    {
        public SerializableData(String name) {
            super(name);
        }
    }

    @XStreamAlias("serializable-data2")
    private static class SerializableData2
        extends Data
        implements Serializable
    {
        private static final long serialVersionUID = 99;

        public SerializableData2(String name) {
            super(name);
        }
    }

    @XStreamAlias("annotation-data")
    @SerialVersion(1)
    private static class AnnotationData
        extends Data
    {
        private AnnotationData(String name) {
            super(name);
        }
    }

    @XStreamAlias("annotation-data2")
    @SerialVersion(2)
    private static class AnnotationData2
        extends AnnotationData
    {
        private AnnotationData2(String name) {
            super(name);
        }
    }
}
