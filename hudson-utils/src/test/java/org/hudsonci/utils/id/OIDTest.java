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

package org.hudsonci.utils.id;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link OID}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class OIDTest
{
    @Test
    public void testSimple() {
        Object obj = new Object();
        OID oid = OID.get(obj);
        assertEquals(obj.toString(), oid.toString());
    }

    @Test
    public void testParse() {
        Object obj = new Object();
        String spec = obj.toString();
        OID oid = OID.parse(spec);
        assertEquals(obj.getClass().getName(), oid.getType());
        assertEquals(obj.hashCode(), oid.getHash());
        assertEquals(spec, oid.toString());
    }

    @Test
    public void testGetNull() {
        OID oid = OID.get(null);
        assertEquals(OID.NULL, oid);
    }
}
