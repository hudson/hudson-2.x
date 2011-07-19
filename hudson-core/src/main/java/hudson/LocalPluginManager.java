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

package hudson;

import hudson.model.Hudson;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link PluginManager}
 *
 * @author Kohsuke Kawaguchi
 */
public class LocalPluginManager extends PluginManager {
    private final Hudson hudson;
    public LocalPluginManager(Hudson hudson) {
        super(hudson.servletContext, new File(hudson.getRootDir(),"plugins"));
        this.hudson = hudson;
    }

    /**
     * If the war file has any "/WEB-INF/plugins/*.hpi", extract them into the plugin directory.
     *
     * @return
     *      File names of the bundled plugins. Like {"ssh-slaves.hpi","subvesrion.hpi"}
     */
    @Override
    protected Collection<String> loadBundledPlugins() {
        // this is used in tests, when we want to override the default bundled plugins with .hpl versions
        if (System.getProperty("hudson.bundled.plugins") != null) {
            return Collections.emptySet();
        }

        Set<String> names = new HashSet<String>();

        for( String path : Util.fixNull((Set<String>)hudson.servletContext.getResourcePaths("/WEB-INF/plugins"))) {
            String fileName = path.substring(path.lastIndexOf('/')+1);
            if(fileName.length()==0) {
                // see http://www.nabble.com/404-Not-Found-error-when-clicking-on-help-td24508544.html
                // I suspect some containers are returning directory names.
                continue;
            }
            try {
                names.add(fileName);

                URL url = hudson.servletContext.getResource(path);
                copyBundledPlugin(url, fileName);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to extract the bundled plugin "+fileName,e);
            }
        }

        return names;
    }

    private static final Logger LOGGER = Logger.getLogger(LocalPluginManager.class.getName());
}
