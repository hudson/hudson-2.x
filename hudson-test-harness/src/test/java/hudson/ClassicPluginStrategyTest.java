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
 *    Kohsuke Kawaguchi,   Alan Harder
 *     
 *
 *******************************************************************************/ 

package hudson;

import hudson.PluginWrapper;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.recipes.LocalData;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author Alan Harder
 */
public class ClassicPluginStrategyTest extends HudsonTestCase {

    @Override
    protected void setUp() throws Exception {
        useLocalPluginManager = true;
        super.setUp();
    }

    /**
     * Test finding resources via DependencyClassLoader.
     */
    @LocalData
    public void testDependencyClassLoader() throws Exception {
        // Test data has: foo3 depends on foo2,foo1; foo2 depends on foo1
        // (thus findResources from foo3 can find foo1 resources via 2 dependency paths)
        PluginWrapper p = hudson.getPluginManager().getPlugin("foo3");
        String res = p.getIndexPage().toString();
        assertTrue(res + "should be foo3", res.contains("/foo3/"));

        // In the current impl, the dependencies are the parent ClassLoader so resources
        // are found there before checking the plugin itself.  Adjust the expected results
        // below if this is ever changed to check the plugin first.
        Enumeration<URL> en = p.classLoader.getResources("index.jelly");
        for (int i = 0; en.hasMoreElements(); i++) {
            res = en.nextElement().toString();
            if (i < 2)
                assertTrue("In current impl, " + res + "should be foo1 or foo2",
                           res.contains("/foo1/") || res.contains("/foo2/"));
            else
                assertTrue("In current impl, " + res + "should be foo3", res.contains("/foo3/"));
        }
        res = p.classLoader.getResource("index.jelly").toString();
        assertTrue("In current impl, " + res + " should be foo1 or foo2",
                   res.contains("/foo1/") || res.contains("/foo2/"));
    }
}
