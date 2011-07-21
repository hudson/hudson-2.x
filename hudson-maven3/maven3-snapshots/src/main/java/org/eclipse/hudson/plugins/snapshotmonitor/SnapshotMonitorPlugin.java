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

package org.eclipse.hudson.plugins.snapshotmonitor;

import hudson.Plugin;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Items;
import net.sf.json.JSONObject;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.hudson.utils.plugin.ui.JellyAccessible;
import org.kohsuke.stapler.StaplerRequest;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Hudson maven snapshot monitor plugin.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class SnapshotMonitorPlugin
    extends Plugin
{
    /** The Maven repository URL; required. */
    private String url;

    /** The username to authenticate with; optional. */
    private String userName;

    /** The password to authenticate with; optional. */
    private String password;

    @JellyAccessible
    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    @JellyAccessible
    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    @JellyAccessible
    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public boolean isConfigured() {
        return getUrl() != null;
    }
    
    @Override
    public void start() throws Exception {
        Hudson.XSTREAM.alias("org.hudsonci.plugins.snapshotmonitor.SnapshotMonitorPlugin", SnapshotMonitorPlugin.class);
        Items.XSTREAM.processAnnotations(new Class[] {
            SnapshotTrigger.class, WatchedDependenciesProperty.class
        });
        load();
    }

    @Override
    public void configure(final StaplerRequest req, final JSONObject data)
        throws IOException, ServletException, Descriptor.FormException
    {
        assert req != null;
        req.bindJSON(this, data);

        // Sanity check for nulls manually
        if (StringUtils.isEmpty(url)) {
            url = null;
        }
        if (StringUtils.isEmpty(userName)) {
            userName = null;
        }
        if (StringUtils.isEmpty(password)) {
            password = null;
        }

        save();
    }

}
