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
 * Verify equals and hashCode methods for Shell object.
 * <p/>
 * Date: 10/5/11
 *
 * @author Nikita Levyankov
 */
public class ShellEqualsHashCodeTest {

    @Test
    public void testHashCode() {
        assertEquals(new Shell(null).hashCode(), new Shell(null).hashCode());
        assertEquals(new Shell("").hashCode(), new Shell("").hashCode());
        assertEquals(new Shell("echo").hashCode(), new Shell("echo").hashCode());
        assertFalse(new Shell("echo 'test'").hashCode() == new Shell("echo '123'").hashCode());
        assertFalse(new Shell(null).hashCode() == new Shell("echo '123'").hashCode());
    }

    @Test
    public void testEqual() {
        Shell echo = new Shell("echo");
        assertEquals(echo, echo);
        assertFalse(new Shell("echo").equals(null));
        assertFalse(echo.equals(new Object()));
        assertEquals(echo, new Shell("echo"));
        assertEquals(new Shell(null), new Shell(null));
        assertEquals(new Shell(""), new Shell(""));
        assertFalse(new Shell("echo 'test'").equals(new Shell("echo '123'")));
        assertFalse(new Shell(null).equals(new Shell("echo '123'")));
    }
}
