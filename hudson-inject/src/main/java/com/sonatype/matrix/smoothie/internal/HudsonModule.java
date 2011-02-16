/**
 * The MIT License
 *
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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

package com.sonatype.matrix.smoothie.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import hudson.PluginManager;
import hudson.TcpSlaveAgentListener;
import hudson.model.FingerprintMap;
import hudson.model.Hudson;
import hudson.model.Queue;
import hudson.model.User;
import hudson.security.SecurityRealm;

import javax.inject.Named;

/**
 * Configuration of bindings for Hudson components.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.2
 */
public class HudsonModule
    extends AbstractModule
{
    @Override
    protected void configure() {
        // ???
    }

    @Provides
    private Hudson getHudson() {
        Hudson hudson = Hudson.getInstance();
        if (hudson == null) {
            throw new IllegalStateException();
        }
        return hudson;
    }

    private static PluginManager plugins;

    public static void bind(final PluginManager plugins) {
        assert plugins != null;
        HudsonModule.plugins = plugins;
    }

    @Provides
    private PluginManager getPluginManager() {
        PluginManager target = plugins != null ? plugins : getHudson().getPluginManager();
        if (target == null) {
            throw new IllegalStateException();
        }
        return target;
    }

    // Helpers to access Hudson singletons via injection

    @Provides
    private SecurityRealm getSecurityRealm() {
        return getHudson().getSecurityRealm();
    }

    @Provides
    private Queue getQueue() {
        return getHudson().getQueue();
    }

    @Provides
    private TcpSlaveAgentListener getTcpSlaveAgentListener() {
        return getHudson().getTcpSlaveAgentListener();
    }
    
    @Provides
    private FingerprintMap getFingerprintMap() {
        return getHudson().getFingerprintMap();
    }

    @Provides
    @Named("current")
    private User getCurrentUser() {
        return User.current();
    }
}