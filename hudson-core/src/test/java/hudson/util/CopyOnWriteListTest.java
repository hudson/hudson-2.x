/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.util;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi, Alan Harder
 */
public class CopyOnWriteListTest extends TestCase {
    public static final class TestData {
        CopyOnWriteList list1 = new CopyOnWriteList();
        List list2 = new ArrayList();
    }

    /**
     * Verify that the serialization form of List and CopyOnWriteList are the same.
     */
    public void testSerialization() throws Exception {
        XStream2 xs = new XStream2();
        TestData td = new TestData();

        String out = xs.toXML(td);
        assertEquals("empty lists", "<hudson.util.CopyOnWriteListTest_-TestData>"
                + "<list1/><list2/></hudson.util.CopyOnWriteListTest_-TestData>",
                out.replaceAll("\\s+", ""));
        TestData td2 = (TestData)xs.fromXML(out.toString());
        assertTrue(td2.list1.isEmpty());
        assertTrue(td2.list2.isEmpty());

        td.list1.add("foobar1");
        td.list2.add("foobar2");
        out = xs.toXML(td);
        assertEquals("lists", "<hudson.util.CopyOnWriteListTest_-TestData>"
                + "<list1><string>foobar1</string></list1><list2><string>foobar2"
                + "</string></list2></hudson.util.CopyOnWriteListTest_-TestData>",
                out.replaceAll("\\s+", ""));
        td2 = (TestData)xs.fromXML(out.toString());
        assertEquals("foobar1", td2.list1.getView().get(0));
        assertEquals("foobar2", td2.list2.get(0));
    }
}
