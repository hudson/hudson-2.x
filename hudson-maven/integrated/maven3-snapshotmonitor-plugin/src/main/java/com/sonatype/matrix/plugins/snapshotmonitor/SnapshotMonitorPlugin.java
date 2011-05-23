/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.plugins.snapshotmonitor;

import com.sonatype.matrix.ui.JellyAccessible;
import hudson.Plugin;
import hudson.model.Descriptor;
import hudson.model.Items;
import net.sf.json.JSONObject;
import org.codehaus.plexus.util.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Matrix snapshot monitor plugin.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.2
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