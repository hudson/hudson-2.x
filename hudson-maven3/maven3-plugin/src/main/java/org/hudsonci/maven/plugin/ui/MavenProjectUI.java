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

import org.hudsonci.maven.plugin.builder.MavenProjectAction;

import org.hudsonci.utils.plugin.ui.JellyAccessible;
import org.hudsonci.utils.plugin.ui.UIComponentSupport;
import hudson.model.AbstractProject;
import hudson.security.Permission;

/**
 * Provides the UI support to display the Maven details for a project.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class MavenProjectUI
    extends UIComponentSupport<MavenProjectAction>
{
    public MavenProjectUI(final MavenProjectAction parent) {
        super(parent);
    }

    public String getIconFileName() {
        return getIconFileName("maven-icon-24x24.png");
    }

    public String getDisplayName() {
        return "Maven 3";
    }

    public String getUrlName() {
        return "maven";
    }

    public Object getSidePanelOwner() {
        return this;
    }

    @JellyAccessible
    public AbstractProject getProject() {
        return getParent().getProject();
    }

    @JellyAccessible
    public boolean isBuilding() {
        return getProject().isBuilding();
    }

    @JellyAccessible
    public boolean isBuildAvailable() {
        return getProject().getLastBuild() != null;
    }

    public Permission getViewPermission() {
        return Permission.READ;
    }

    protected void checkPermission(final Permission perm) {
        getProject().checkPermission(perm);
    }
}
