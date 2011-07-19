/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *       
 *
 *******************************************************************************/ 

package hudson.util;

import hudson.XmlFile;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kohsuke Kawaguchi
 */
public class XStreamTest extends TestCase {
    public static class Foo {
        ConcurrentHashMap m = new ConcurrentHashMap();
    }

    public void testConcurrentHashMapSerialization() throws Exception {
        Foo foo = new Foo();
        foo.m.put("abc","def");
        foo.m.put("ghi","jkl");
        File v = File.createTempFile("hashmap", "xml");
        try {
            new XmlFile(v).write(foo);

            // should serialize like map
            String xml = FileUtils.readFileToString(v);
            assertFalse(xml.contains("java.util.concurrent"));
            System.out.println(xml);
        } finally {
            v.delete();
        }

        // should be able to read in old data just fine
        Foo map = (Foo)new XStream2().fromXML(getClass().getResourceAsStream("old-concurrentHashMap.xml"));
        assertEquals("def",map.m.get("abc"));
    }
}
