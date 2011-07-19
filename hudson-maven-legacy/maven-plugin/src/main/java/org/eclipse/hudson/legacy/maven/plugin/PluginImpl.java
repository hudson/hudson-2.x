/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin;

import hudson.Plugin;
import hudson.maven.MavenModuleSet;
import hudson.model.Items;

/**
 * @author huybrechts
 */
public class PluginImpl extends Plugin {
    @Override
    public void start() throws Exception {
        super.start();

        Items.XSTREAM.alias("maven2", MavenModule.class);
        Items.XSTREAM.alias("dependency", ModuleDependency.class);
        Items.XSTREAM.alias("maven2-module-set", MavenModule.class);  // this was a bug, but now we need to keep it for compatibility
        Items.XSTREAM.alias("maven2-moduleset", MavenModuleSet.class);
    }

}
