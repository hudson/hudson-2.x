/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui;

import hudson.model.ManagementLink;
import org.kohsuke.stapler.StaplerProxy;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Provides the global Maven configuration management link.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Named
@Singleton
public class MavenConfigurationLink
    extends ManagementLink
    implements StaplerProxy
{
    private MavenConfigurationUI ui;

    @Override
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

    @Override
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