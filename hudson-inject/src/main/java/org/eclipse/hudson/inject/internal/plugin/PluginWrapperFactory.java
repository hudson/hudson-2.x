/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.eclipse.hudson.inject.internal.plugin;

import hudson.ClassicPluginStrategy;
import hudson.PluginManager;
import hudson.PluginStrategy;
import hudson.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides {@link PluginWrapper} creation facilities.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.397
 */
@Named
@Singleton
public class PluginWrapperFactory
{
    private static final Logger log = LoggerFactory.getLogger(PluginWrapperFactory.class);

    private final PluginStrategy delegate;

    @Inject
    public PluginWrapperFactory(final PluginManager plugins) {
        checkNotNull(plugins);

        // Using the Classic strategy to build the wrapper, since its not easy to re-implement its logic
        this.delegate = new ClassicPluginStrategy(plugins)
        {
            @Override
            protected ClassLoader createClassLoader(List<File> files, ClassLoader parent, Attributes atts) throws IOException {
                assert files != null;
                List<URL> urls = new ArrayList<URL>(files.size());
                for (File file : files) {
                    urls.add(file.toURI().toURL());
                }
                return new PluginClassLoader(urls, parent);
            }
        };
    }

    public PluginWrapper create(final File file) throws Exception {
        checkNotNull(file);
        log.trace("Creating plugin wrapper for: {}", file);

        PluginWrapper plugin = delegate.createPluginWrapper(file);

        // Attach the plugin to the class-loader
        PluginClassLoader cl = (PluginClassLoader) plugin.classLoader;
        cl.setPlugin(plugin);

        return plugin;
    }
}
