/*******************************************************************************
 *
 * Copyright (c) 2010 Yahoo! Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Alan Harder
 *       
 *
 *******************************************************************************/ 

package hudson.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import junit.framework.TestCase;

/**
 * @author Mike Dillon, Alan Harder
 */
public class CopyOnWriteMapTest extends TestCase {
    public static final class HashData {
        CopyOnWriteMap.Hash<String,String> map1 = new CopyOnWriteMap.Hash<String,String>();
        HashMap<String,String> map2 = new HashMap<String,String>();
    }

    /**
     * Verify that serialization form of CopyOnWriteMap.Hash and HashMap are the same.
     */
    public void testHashSerialization() throws Exception {
        HashData td = new HashData();
        XStream2 xs = new XStream2();

        String out = xs.toXML(td);
        assertEquals("empty maps", "<hudson.util.CopyOnWriteMapTest_-HashData>"
                + "<map1/><map2/></hudson.util.CopyOnWriteMapTest_-HashData>",
                out.replaceAll("\\s+", ""));
        HashData td2 = (HashData)xs.fromXML(out);
        assertTrue(td2.map1.isEmpty());
        assertTrue(td2.map2.isEmpty());

        td.map1.put("foo1", "bar1");
        td.map2.put("foo2", "bar2");
        out = xs.toXML(td);
        assertEquals("maps", "<hudson.util.CopyOnWriteMapTest_-HashData><map1>"
                + "<entry><string>foo1</string><string>bar1</string></entry></map1>"
                + "<map2><entry><string>foo2</string><string>bar2</string></entry>"
                + "</map2></hudson.util.CopyOnWriteMapTest_-HashData>",
                out.replaceAll("\\s+", ""));
        td2 = (HashData)xs.fromXML(out);
        assertEquals("bar1", td2.map1.get("foo1"));
        assertEquals("bar2", td2.map2.get("foo2"));
    }

    public static final class TreeData {
        CopyOnWriteMap.Tree<String,String> map1;
        TreeMap<String,String> map2;
        TreeData() {
            map1 = new CopyOnWriteMap.Tree<String,String>();
            map2 = new TreeMap<String,String>();
        }
        TreeData(Comparator<String> comparator) {
            map1 = new CopyOnWriteMap.Tree<String,String>(comparator);
            map2 = new TreeMap<String,String>(comparator);
        }
    }

    /**
     * Verify that an empty CopyOnWriteMap.Tree can be serialized,
     * and that serialization form is the same as a standard TreeMap.
     */
    public void testTreeSerialization() throws Exception {
        TreeData td = new TreeData();
        XStream2 xs = new XStream2();

        String out = xs.toXML(td);
        assertEquals("empty maps", "<hudson.util.CopyOnWriteMapTest_-TreeData>"
                + "<map1/><map2/>"
                + "</hudson.util.CopyOnWriteMapTest_-TreeData>",
                out.replaceAll("\\s+", ""));
        TreeData td2 = (TreeData)xs.fromXML(out);
        assertTrue(td2.map1.isEmpty());
        assertTrue(td2.map2.isEmpty());

        td = new TreeData(String.CASE_INSENSITIVE_ORDER);
        td.map1.put("foo1", "bar1");
        td.map2.put("foo2", "bar2");
        out = xs.toXML(td);
        assertEquals("maps", "<hudson.util.CopyOnWriteMapTest_-TreeData><map1>"
                + "<comparator class=\"java.lang.String$CaseInsensitiveComparator\"/>"
                + "<entry><string>foo1</string><string>bar1</string></entry></map1>"
                + "<map2><comparator class=\"java.lang.String$CaseInsensitiveComparator\""
                + " reference=\"../../map1/comparator\"/>"
                + "<entry><string>foo2</string><string>bar2</string></entry></map2>"
                + "</hudson.util.CopyOnWriteMapTest_-TreeData>",
                out.replaceAll(">\\s+<", "><"));
        td2 = (TreeData)xs.fromXML(out);
        assertEquals("bar1", td2.map1.get("foo1"));
        assertEquals("bar2", td2.map2.get("foo2"));
    }
}
