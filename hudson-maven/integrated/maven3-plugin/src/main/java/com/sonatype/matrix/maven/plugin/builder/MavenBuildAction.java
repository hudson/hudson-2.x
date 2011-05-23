/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder;

import com.sonatype.matrix.maven.plugin.ui.MavenBuildUI;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import org.kohsuke.stapler.StaplerProxy;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides the build's "Maven" link and delegates to {@link com.sonatype.matrix.maven.plugin.ui.MavenBuildUI} for viewing the build's Maven details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@XStreamAlias("maven-build-action")
public class MavenBuildAction
    implements Action, StaplerProxy
{
    private final AbstractBuild<?,?> build;

    @XStreamOmitField
    private MavenBuildUI ui;

    public MavenBuildAction(final AbstractBuild<?, ?> build) {
        this.build = checkNotNull(build);
    }

    public AbstractBuild<?,?> getBuild() {
        return build;
    }

    @Override
    public MavenBuildUI getTarget() {
        if (ui == null) {
            ui = new MavenBuildUI(this);
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
}