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

import org.eclipse.hudson.inject.injecto.Injectable;
import org.eclipse.hudson.maven.plugin.builder.MavenBuildAction;
import org.eclipse.hudson.rest.common.ProjectNameCodec;
import org.eclipse.hudson.utils.plugin.ui.JellyAccessible;
import org.eclipse.hudson.utils.plugin.ui.UIComponentSupport;

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
