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

package org.eclipse.hudson.maven.plugin.ui;

import org.eclipse.hudson.maven.plugin.builder.MavenProjectAction;
import org.eclipse.hudson.utils.plugin.ui.JellyAccessible;
import org.eclipse.hudson.utils.plugin.ui.UIComponentSupport;

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
