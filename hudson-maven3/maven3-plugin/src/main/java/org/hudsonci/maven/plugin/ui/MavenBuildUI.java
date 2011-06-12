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

import org.hudsonci.utils.plugin.ui.JellyAccessible;
import org.hudsonci.utils.plugin.ui.UIComponentSupport;
import org.hudsonci.rest.common.ProjectNameCodec;
import org.hudsonci.inject.injecto.Injectable;
import org.hudsonci.maven.plugin.builder.MavenBuildAction;

import hudson.model.AbstractBuild;
import hudson.model.Item;
import hudson.security.Permission;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides the UI support to display the Maven details for a build.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class MavenBuildUI
    extends UIComponentSupport<MavenBuildAction>
    implements Injectable
{
    private ProjectNameCodec projectNameCodec;

    public MavenBuildUI(final MavenBuildAction parent) {
        super(parent);
    }

    @Inject
    public void setProjectNameCodec(final ProjectNameCodec projectNameCodec) {
        this.projectNameCodec = checkNotNull(projectNameCodec);
    }

    public String getDisplayName() {
        return "Maven 3";
    }

    public String getUrlName() {
        return "maven";
    }

    public String getIconFileName() {
        return getIconFileName("maven-icon-24x24.png");
    }

    public Object getSidePanelOwner() {
        return this;
    }

    public String getPageTitle() {
        return String.format("%s Maven 3 Build Information",
                getBuild().getFullDisplayName());
    }

    @JellyAccessible
    public AbstractBuild getBuild() {
        return getParent().getBuild();
    }

    @JellyAccessible
    public String getProjectName() {
        // TODO: probably want to use the JobUUID to make the paths resilient to job renames, and avoid needing to deal with encoding
        return projectNameCodec.encode(getBuild().getProject().getFullName());
    }

    @JellyAccessible
    public String getBuildNumber() {
        return String.valueOf(getBuild().getNumber());
    }

    public Permission getViewPermission() {
        return Item.READ;
    }

    protected void checkPermission(final Permission perm) {
        getBuild().checkPermission(perm);
    }
}
