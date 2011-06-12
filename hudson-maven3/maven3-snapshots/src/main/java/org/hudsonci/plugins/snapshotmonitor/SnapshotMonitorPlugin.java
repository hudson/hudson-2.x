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

package org.hudsonci.plugins.snapshotmonitor;

import org.hudsonci.utils.plugin.ui.JellyAccessible;
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
