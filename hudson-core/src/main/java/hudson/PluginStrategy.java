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
*    Kohsuke Kawaguchi, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson;

import hudson.model.Hudson;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Pluggability point for how to create {@link PluginWrapper}.
 *
 * <p>
 * This extension point was added to allow plugins to be loaded into a different environment
 * (such as loading it in an existing DI container like Plexus.) A plugin strategy is a singleton
 * instance, and as such this feature is primarily meant for OEM.
 *
 * See {@link PluginManager#createPluginStrategy()} for how this instance is created.
 */
public interface PluginStrategy extends ExtensionPoint {

	/**
	 * Creates a plugin wrapper, which provides a management interface for the plugin
	 * @param archive
     *      Either a directory that points to a pre-exploded plugin, or an hpi file, or an hpl file.
	 */
	public abstract PluginWrapper createPluginWrapper(File archive)
			throws IOException;

	/**
	 * Loads the plugin and starts it.
	 * 
	 * <p>
	 * This should be done after all the classloaders are constructed for all
	 * the plugins, so that dependencies can be properly loaded by plugins.
	 */
	public abstract void load(PluginWrapper wrapper) throws IOException;

	/**
	 * Optionally start services provided by the plugin. Should be called
	 * when all plugins are loaded.
	 * 
	 * @param plugin
	 */
	public abstract void initializeComponents(PluginWrapper plugin);

	/**
	 * Find components of the given type using the assigned strategy.
	 *
	 * @param type The component type
	 * @param hudson The Hudson scope
	 * @return Sequence of components
	 * @since 1.397
	 */
	public abstract <T> List<ExtensionComponent<T>> findComponents(Class<T> type, Hudson hudson);
}
