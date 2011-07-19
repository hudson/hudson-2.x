/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package org.jvnet.hudson.test;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.jvnet.hudson.test.junit.GroupedTest;
import org.kohsuke.stapler.MetaClassLoader;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Builds up a {@link TestSuite} for performing static syntax checks on Jelly scripts.
 *
 * @author Kohsuke Kawaguchi
 */
public class JellyTestSuiteBuilder {
    /**
     * Given a jar file or a class file directory, recursively search all the Jelly files and build a {@link TestSuite}
     * that performs static syntax checks.
     */
    public static TestSuite build(File res) throws Exception {
        TestSuite ts = new JellyTestSuite();

        final JellyClassLoaderTearOff jct = new MetaClassLoader(JellyTestSuiteBuilder.class.getClassLoader()).loadTearOff(JellyClassLoaderTearOff.class);

        if (res.isDirectory()) {
            for (final File jelly : (Collection <File>)FileUtils.listFiles(res,new String[]{"jelly"},true))
                ts.addTest(new JellyCheck(jelly.toURI().toURL(), jct));
        }
        if (res.getName().endsWith(".jar")) {
            String jarUrl = res.toURI().toURL().toExternalForm();
            JarFile jf = new JarFile(res);
            Enumeration<JarEntry> e = jf.entries();
            while (e.hasMoreElements()) {
                JarEntry ent =  e.nextElement();
                if (ent.getName().endsWith(".jelly"))
                    ts.addTest(new JellyCheck(new URL("jar:"+jarUrl+"!/"+ent.getName()), jct));
            }
            jf.close();
        }
        return ts;
    }

    private static class JellyCheck extends TestCase {
        private final URL jelly;
        private final JellyClassLoaderTearOff jct;

        public JellyCheck(URL jelly, JellyClassLoaderTearOff jct) {
            super(jelly.getPath());
            this.jelly = jelly;
            this.jct = jct;
        }

        @Override
        protected void runTest() throws Exception {
            jct.createContext().compileScript(jelly);
            Document dom = new SAXReader().read(jelly);
            checkLabelFor(dom);
            // TODO: what else can we check statically? use of taglibs?
        }

        /**
         * Makes sure that &lt;label for=...> is not used inside config.jelly nor global.jelly
         */
        private void checkLabelFor(Document dom) {
            if (isConfigJelly() || isGlobalJelly()) {
                if (!dom.selectNodes("//label[@for]").isEmpty())
                    throw new AssertionError("<label for=...> shouldn't be used because it doesn't work " +
                            "when the configuration item is repeated. Use <label class=\"attach-previous\"> " +
                            "to have your label attach to the previous DOM node instead.");
            }
        }

        private boolean isConfigJelly() {
            return jelly.toString().endsWith("/config.jelly");
        }

        private boolean isGlobalJelly() {
            return jelly.toString().endsWith("/global.jelly");
        }
    }

    /**
     * Execute all the Jelly tests in a servlet request handling context. To do so, we reuse HudsonTestCase
     */
    private static final class JellyTestSuite extends GroupedTest {
        HudsonTestCase h = new HudsonTestCase("Jelly test wrapper") {};

        @Override
        protected void setUp() throws Exception {
            h.setUp();
        }

        @Override
        protected void tearDown() throws Exception {
            h.tearDown();
        }

        private void doTests(TestResult result) throws Exception {
            super.runGroupedTests(result);
        }

        @Override
        protected void runGroupedTests(final TestResult result) throws Exception {
            h.executeOnServer(new Callable<Object>() {
                // this code now inside a request handling thread
                public Object call() throws Exception {
                    doTests(result);
                    return null;
                }
            });
        }
    }
}
