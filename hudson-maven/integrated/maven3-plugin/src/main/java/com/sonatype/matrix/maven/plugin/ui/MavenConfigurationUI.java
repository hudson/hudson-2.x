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
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.MavenConfigurationEntryPoint;
import hudson.model.Hudson;
import hudson.security.Permission;

/**
 * UI delegate for {@link MavenConfigurationLink}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public class MavenConfigurationUI
    extends UIComponentSupport<MavenConfigurationLink>
{
    public MavenConfigurationUI(final MavenConfigurationLink parent) {
        super(parent);
    }

    @Override
    public String getIconFileName() {
        return getIconFileName("maven-icon-48x48.png");
    }

    @Override
    public String getUrlName() {
        return "maven";
    }

    @Override
    public String getDisplayName() {
        return "Maven Configuration";
    }

    public String getDescription() {
        return "Manage Maven global configuration options.";
    }

    @JellyAccessible
    public String getMainPanelId() {
        return MavenConfigurationEntryPoint.MAIN_PANEL_ID;
    }

    @Override
    public Permission getViewPermission() {
        return Hudson.ADMINISTER;
    }
}