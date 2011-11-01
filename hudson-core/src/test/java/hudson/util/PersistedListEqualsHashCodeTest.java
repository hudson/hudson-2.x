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

import hudson.model.FreeStyleProjectMock;
import hudson.model.Saveable;
import hudson.tasks.Shell;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Verify equals and hashCode methods for PersistedList object.
 * <p/>
 * Date: 10/5/11
 *
 * @author Nikita Levyankov
 */
public class PersistedListEqualsHashCodeTest {

    private Saveable owner1 = new FreeStyleProjectMock("test1");
    private Saveable owner2 = new FreeStyleProjectMock("test1");

    private PersistedList persistedList1;
    private PersistedList persistedList2;
    private PersistedList persistedList3;

    @Before
    public void setUp() {
        persistedList1 = new PersistedList(owner1);
        persistedList2 = new PersistedList(owner2);
        persistedList3 = new PersistedList(owner1);
    }

    @Test
    public void testHashCode() throws IOException {
        assertEquals(new PersistedList().hashCode(), new PersistedList().hashCode());
        assertEquals(persistedList1.hashCode(), new PersistedList(owner1).hashCode());
        assertFalse(persistedList1.hashCode() == persistedList2.hashCode());
        persistedList1.add(new Shell("echo 'test1'"));
        assertFalse(persistedList1.hashCode() == persistedList2.hashCode());
        persistedList2.add(new Shell("echo 'test1'"));
        assertFalse(persistedList1.hashCode() == persistedList2.hashCode());
        persistedList3.add(new Shell("echo 'test1'"));
        assertEquals(persistedList1.hashCode(), persistedList3.hashCode());
        persistedList3.replaceBy(Arrays.asList(new Shell("echo 'test2'")));
        assertFalse(persistedList1.hashCode() == persistedList3.hashCode());
    }

    @Test
    public void testEqual() throws IOException {
        assertEquals(persistedList1, persistedList1);
        assertFalse(persistedList1.equals(null));
        assertFalse(persistedList1.equals(new Object()));
        assertFalse(persistedList1.equals(persistedList2));
        assertEquals(persistedList1, persistedList3);
        persistedList1.add(new Shell("echo 'test1'"));
        persistedList3.add(new Shell("echo 'test1'"));
        assertEquals(persistedList1, persistedList3);
        persistedList1.add(new Shell("echo 'test3'"));
        persistedList1.add(new Shell("echo 'test2'"));
        persistedList3.add(new Shell("echo 'test2'"));
        persistedList3.add(new Shell("echo 'test3'"));
        assertEquals(persistedList1, persistedList3);

        persistedList3.replaceBy(Arrays.asList(new Shell("echo 'test2'")));
        assertFalse(persistedList1.equals(persistedList3));
    }
}
