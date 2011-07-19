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

import junit.framework.TestCase;

/**
 * 
 * @author Kohsuke Kawaguchi
 */
public class SubClassGeneratorTest extends TestCase {
    public static class Foo {
        String s;
        double x;
        int y;
        public Foo() {}
        public Foo(String s) {this.s=s;}
        public Foo(double x, int y) {this.x=x;this.y=y;}
    }
    public void testFoo() throws Exception {
        Class<? extends Foo> c = new SubClassGenerator(getClass().getClassLoader()).generate(Foo.class, "12345");
        assertEquals("12345",c.getName());

        c.newInstance();

        Foo f = c.getConstructor(String.class).newInstance("aaa");
        assertEquals("aaa",f.s);

        f = c.getConstructor(double.class,int.class).newInstance(1.0,3);
        assertEquals(1.0,f.x);
        assertEquals(3,f.y);
    }
}
