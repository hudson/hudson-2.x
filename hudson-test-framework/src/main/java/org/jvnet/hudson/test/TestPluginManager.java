/*******************************************************************************
 *
 * Copyright (c) 2010, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *       Kohsuke Kawaguchi
 *
 *******************************************************************************/ 

package org.jvnet.hudson.test;

import hudson.Plugin;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link PluginManager} to speed up unit tests.
 *
 * <p>
 * Instead of loading every plugin for every test case, this allows them to reuse a single plugin manager.
 *
 * <p>
 * TODO: {@link Plugin} start/stop/postInitialize invocation semantics gets different. Perhaps
 * 
 * @author Kohsuke Kawaguchi
 * @see HudsonTestCase#useLocalPluginManager
 */
public class TestPluginManager extends PluginManager {
    public static final PluginManager INSTANCE;

    private TestPluginManager() throws IOException {
        // TestPluginManager outlives a Jetty server, so can't pass in ServletContext.
        super(null, Util.createTempDir());
    }

    @Override
    protected Collection<String> loadBundledPlugins() throws Exception {
        Set<String> names = new HashSet<String>();

        File[] children = new File(WarExploder.getExplodedDir(),"WEB-INF/plugins").listFiles();
        for (File child : children) {
            try {
                names.add(child.getName());

                copyBundledPlugin(child.toURI().toURL(), child.getName());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to extract the bundled plugin "+child,e);
            }
        }
        // If running tests for a plugin, include the plugin being tested
        URL u = getClass().getClassLoader().getResource("the.hpl");
        if (u!=null) try {
            names.add("the.hpl");
            copyBundledPlugin(u, "the.hpl");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to copy the.hpl",e);
        }

        // and pick up test dependency *.hpi that are placed by maven-hpi-plugin TestDependencyMojo.
        // and copy them into $HUDSON_HOME/plugins.
        URL index = getClass().getResource("/test-dependencies/index");
        if (index!=null) {// if built with maven-hpi-plugin < 1.52 this file won't exist.
            BufferedReader r = new BufferedReader(new InputStreamReader(index.openStream(),"UTF-8"));
            String line;
            while ((line=r.readLine())!=null) {
                copyBundledPlugin(new URL(index, line + ".hpi"), line + ".hpi");
            }
        }

        return names;
    }
    
    @Override
    public void stop() {
        for (PluginWrapper p : activePlugins)
            p.stop();
    }

    private static final Logger LOGGER = Logger.getLogger(TestPluginManager.class.getName());

    static {
        try {
            INSTANCE = new TestPluginManager();
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
