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
package hudson.util;

import hudson.tasks.Builder;
import hudson.tasks.Shell;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Verify equals and hashCode methods for CopyOnWriteList object.
 * <p/>
 * Date: 10/5/11
 *
 * @author Nikita Levyankov
 */
public class CopyOnWriteListEqualsHashCodeTest {
    private List<Builder> data1;
    private List<Builder> data2;

    @Before
    public void setUp() {
        data1 = new ArrayList<Builder>();
        data1.add(new Shell("echo 'test'"));
        data1.add(new Shell("echo 'test1'"));
        data1.add(new Shell("echo 'test2'"));

        data2 = new ArrayList<Builder>();
        data2.add(new Shell("echo 'test1'"));
        data2.add(new Shell("echo 'test'"));
    }

    @Test
    public void testHashCode() {
        assertEquals(new CopyOnWriteList(data1).hashCode(), new CopyOnWriteList(data1).hashCode());

        assertFalse(new CopyOnWriteList(data1).hashCode() == new CopyOnWriteList(data2).hashCode());
        data2.add(new Shell("echo 'test2'"));
        assertFalse(new CopyOnWriteList(data1).hashCode() == new CopyOnWriteList(data2).hashCode());
    }

    @Test
    public void testEqual() {
        CopyOnWriteList list = new CopyOnWriteList();
        assertEquals(list, list);
        assertFalse(list.equals(null));
        assertFalse(list.equals(new Object()));
        assertEquals(new CopyOnWriteList(new ArrayList()), new CopyOnWriteList(new ArrayList()));

        assertFalse(new CopyOnWriteList(data1).equals(new CopyOnWriteList(data2)));
        assertEquals(new CopyOnWriteList(data1), new CopyOnWriteList(data1));
        data2.add(new Shell("echo 'test2'"));
        assertEquals(new CopyOnWriteList(data1), new CopyOnWriteList(data2));
    }
}
