/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui;

import com.sonatype.matrix.ui.JellyAccessible;
import com.sonatype.matrix.ui.UIComponentSupport;
import com.sonatype.matrix.maven.plugin.builder.MavenProjectAction;
import hudson.model.AbstractProject;
import hudson.security.Permission;

/**
 * Provides the UI support to display the Maven details for a project.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public class MavenProjectUI
    extends UIComponentSupport<MavenProjectAction>
{
    public MavenProjectUI(final MavenProjectAction parent) {
        super(parent);
    }

    @Override
    public String getIconFileName() {
        return getIconFileName("maven-icon-24x24.png");
    }

    @Override
    public String getDisplayName() {
        return "Maven";
    }

    @Override
    public String getUrlName() {
        return "maven";
    }

    @Override
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

    @Override
    public Permission getViewPermission() {
        return Permission.READ;
    }

    @Override
    protected void checkPermission(final Permission perm) {
        getProject().checkPermission(perm);
    }
}