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

package org.eclipse.hudson.maven.plugin.builder;

import hudson.model.AbstractProject;
import hudson.model.Action;

import org.eclipse.hudson.maven.plugin.ui.MavenProjectUI;
import org.kohsuke.stapler.StaplerProxy;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides the project's "Maven" link and delegates to {@link org.eclipse.hudson.maven.plugin.ui.MavenProjectUI} for viewing the project's Maven details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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

    public MavenProjectUI getTarget() {
        if (ui == null) {
            ui = new MavenProjectUI(this);
        }
        return ui;
    }

    // FIXME: Making invisible action until we have a project GWT view available to display

    public String getIconFileName() {
        //return getTarget().getIconFileName();
        return null;
    }

    public String getDisplayName() {
        //return getTarget().getDisplayName();
        return null;
    }

    public String getUrlName() {
        //return getTarget().getUrlName();
        return null;
    }
}
