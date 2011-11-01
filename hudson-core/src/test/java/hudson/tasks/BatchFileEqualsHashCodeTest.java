/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Nikita Levyankov
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
package hudson.tasks;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Verify equals and hashCode methods for BatchFile object.
 * <p/>
 * Date: 10/5/11
 *
 * @author Nikita Levyankov
 */
public class BatchFileEqualsHashCodeTest {
    @Test
    public void testHashCode() {
        assertEquals(new BatchFile(null).hashCode(), new BatchFile(null).hashCode());
        assertEquals(new BatchFile("").hashCode(), new BatchFile("").hashCode());
        assertEquals(new BatchFile("echo").hashCode(), new BatchFile("echo").hashCode());
        assertFalse(new BatchFile("echo 'test'").hashCode() == new BatchFile("echo '123'").hashCode());
        assertFalse(new BatchFile(null).hashCode() == new BatchFile("echo '123'").hashCode());
    }

    @Test
    public void testEqual() {
        BatchFile echo = new BatchFile("echo");
        assertEquals(echo, echo);
        assertFalse(new BatchFile("echo").equals(null));
        assertFalse(echo.equals(new Object()));
        assertEquals(echo, new BatchFile("echo"));
        assertEquals(new BatchFile(null), new BatchFile(null));
        assertEquals(new BatchFile(""), new BatchFile(""));
        assertFalse(new BatchFile("echo 'test'").equals(new BatchFile("echo '123'")));
        assertFalse(new BatchFile(null).equals(new BatchFile("echo '123'")));
    }
}
