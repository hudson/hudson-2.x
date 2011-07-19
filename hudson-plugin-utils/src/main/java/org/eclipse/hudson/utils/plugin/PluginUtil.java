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

package org.eclipse.hudson.utils.plugin;

import hudson.Plugin;
import hudson.PluginWrapper;

import java.lang.reflect.Field;

/**
 * {@link Plugin} utilities.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class PluginUtil
{
    public static PluginWrapper getWrapper(final Plugin plugin) {
        assert plugin != null;
        try {
            Field field = Plugin.class.getDeclaredField("wrapper");
            field.setAccessible(true);
            return (PluginWrapper) field.get(plugin);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to access PluginWrapper", e);
        }
    }

    public static String getShortName(final Plugin plugin) {
        return getWrapper(plugin).getShortName();
    }

    //
    // FIXME: These do not take into account the context path, not sure if they should or not
    //
    
    public static String getPath(final Plugin plugin) {
        return String.format("/plugin/%s", getShortName(plugin));
    }

    public static String getHelpPath(final Plugin plugin) {
        return String.format("%s/help", getPath(plugin));
    }

    public static String getImagesPath(final Plugin plugin) {
        return String.format("%s/images", getPath(plugin));
    }
}
