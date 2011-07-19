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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import hudson.model.AbstractBuild;
import hudson.model.Action;

import org.eclipse.hudson.maven.plugin.ui.MavenBuildUI;
import org.kohsuke.stapler.StaplerProxy;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides the build's "Maven" link and delegates to {@link org.eclipse.hudson.maven.plugin.ui.MavenBuildUI} for viewing the build's Maven details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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

    public MavenBuildUI getTarget() {
        if (ui == null) {
            ui = new MavenBuildUI(this);
        }
        return ui;
    }

    public String getIconFileName() {
        return getTarget().getIconFileName();
    }

    public String getDisplayName() {
        return getTarget().getDisplayName();
    }

    public String getUrlName() {
        return getTarget().getUrlName();
    }
}
