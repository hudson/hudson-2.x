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
import com.sonatype.matrix.maven.plugin.builder.MavenBuildAction;
import com.sonatype.matrix.rest.common.ProjectNameCodec;
import org.hudsonci.inject.injecto.Injectable;
import hudson.model.AbstractBuild;
import hudson.model.Item;
import hudson.security.Permission;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides the UI support to display the Maven details for a build.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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

    @Override
    public String getDisplayName() {
        return "Maven";
    }

    @Override
    public String getUrlName() {
        return "maven";
    }

    @Override
    public String getIconFileName() {
        return getIconFileName("maven-icon-24x24.png");
    }

    @Override
    public Object getSidePanelOwner() {
        return this;
    }

    @Override
    public String getPageTitle() {
        return String.format("%s Maven Build Information", getBuild().getFullDisplayName());
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

    @Override
    public Permission getViewPermission() {
        return Item.READ;
    }

    @Override
    protected void checkPermission(final Permission perm) {
        getBuild().checkPermission(perm);
    }
}
