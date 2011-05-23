/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder;

import com.sonatype.matrix.maven.plugin.ui.MavenProjectUI;
import hudson.model.AbstractProject;
import hudson.model.Action;
import org.kohsuke.stapler.StaplerProxy;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides the project's "Maven" link and delegates to {@link com.sonatype.matrix.maven.plugin.ui.MavenProjectUI} for viewing the project's Maven details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public class MavenProjectAction
    implements Action, StaplerProxy
{
    private final AbstractProject<?,?> project;

    private MavenProjectUI ui;

    public MavenProjectAction(final AbstractProject<?, ?> project) {
        this.project = checkNotNull(project);
    }

    public AbstractProject<?,?> getProject() {
        return project;
    }

    @Override
    public MavenProjectUI getTarget() {
        if (ui == null) {
            ui = new MavenProjectUI(this);
        }
        return ui;
    }

    // FIXME: Making invisible action until we have a project GWT view available to display

    @Override
    public String getIconFileName() {
        //return getTarget().getIconFileName();
        return null;
    }

    @Override
    public String getDisplayName() {
        //return getTarget().getDisplayName();
        return null;
    }

    @Override
    public String getUrlName() {
        //return getTarget().getUrlName();
        return null;
    }
}