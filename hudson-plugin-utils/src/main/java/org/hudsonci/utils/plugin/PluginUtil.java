/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.utils.plugin;

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
