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

package org.hudsonci.utils.marshal.xref;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Tests for {@link XReference}.
 */
public class XReferenceTest
{
    private XStream xs;

    @Before
    public void setUp() throws Exception {
        xs = new XStream();
        xs.autodetectAnnotations(true);
    }

    @After
    public void tearDown() throws Exception {
        xs = null;
    }

    @Test
    public void testMarshal() throws Exception {
        Date value1 = new Date();
        TestRef ref1 = new TestRef(value1, "a");

        String xml = xs.toXML(ref1);
        System.out.println("XML:\n" + xml);
    }

    @XStreamAlias("test-ref")
    private static class TestRef
        extends XReference
    {
        private String path;

        private TestRef(Object value, String path) {
            super(value);
            this.path = path;
        }

        @Override
        public String getPath() {
            return path;
        }
    }
}
