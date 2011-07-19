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

import hudson.model.ManagementLink;
import org.kohsuke.stapler.StaplerProxy;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Provides the global Maven configuration management link.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class MavenConfigurationLink
    extends ManagementLink
    implements StaplerProxy
{
    private MavenConfigurationUI ui;

    public MavenConfigurationUI getTarget() {
        if (ui == null) {
            ui = new MavenConfigurationUI(this);
        }
        return ui;
    }

    @Override
    public String getIconFileName() {
        return getTarget().getIconFileName();
    }

    public String getDisplayName() {
        return getTarget().getDisplayName();
    }

    @Override
    public String getUrlName() {
        return getTarget().getUrlName();
    }

    @Override
    public String getDescription() {
        return getTarget().getDescription();
    }
}
