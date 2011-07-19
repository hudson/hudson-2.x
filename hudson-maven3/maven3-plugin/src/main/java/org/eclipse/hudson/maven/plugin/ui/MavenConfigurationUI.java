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

import org.eclipse.hudson.maven.plugin.ui.gwt.configure.MavenConfigurationEntryPoint;
import org.eclipse.hudson.utils.plugin.ui.JellyAccessible;
import org.eclipse.hudson.utils.plugin.ui.UIComponentSupport;

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
