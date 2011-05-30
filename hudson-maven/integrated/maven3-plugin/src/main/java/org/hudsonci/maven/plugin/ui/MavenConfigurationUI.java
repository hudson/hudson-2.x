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

package org.hudsonci.maven.plugin.ui;

import org.hudsonci.maven.plugin.ui.gwt.configure.MavenConfigurationEntryPoint;

import org.hudsonci.utils.plugin.ui.JellyAccessible;
import org.hudsonci.utils.plugin.ui.UIComponentSupport;
import hudson.model.Hudson;
import hudson.security.Permission;

/**
 * UI delegate for {@link MavenConfigurationLink}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class MavenConfigurationUI
    extends UIComponentSupport<MavenConfigurationLink>
{
    public MavenConfigurationUI(final MavenConfigurationLink parent) {
        super(parent);
    }

    public String getIconFileName() {
        return getIconFileName("maven-icon-48x48.png");
    }

    public String getUrlName() {
        return "maven";
    }

    public String getDisplayName() {
        return "Maven 3 Configuration";
    }

    public String getDescription() {
        return "Manage Maven 3 global configuration options.";
    }

    @JellyAccessible
    public String getMainPanelId() {
        return MavenConfigurationEntryPoint.MAIN_PANEL_ID;
    }

    public Permission getViewPermission() {
        return Hudson.ADMINISTER;
    }
}
