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

package org.eclipse.hudson.maven.plugin.ui.gwt.configure;

import com.google.gwt.user.client.ui.HasWidgets;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.hudson.maven.plugin.ui.gwt.configure.workspace.WorkspaceManagerPresenter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Controls the MavenConfiguration module.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Singleton
public class MavenConfigurationController
{
    // FIXME: Not sure this is needed, keeping around for now until we have a better way to glue together modules for plugins and pages and such inside of Hudson

    private final WorkspaceManagerPresenter workspaceManagerPresenter;

    @Inject
    public MavenConfigurationController(final WorkspaceManagerPresenter workspaceManagerPresenter) {
        this.workspaceManagerPresenter = checkNotNull(workspaceManagerPresenter);
    }

    public void start(final HasWidgets container) {
        workspaceManagerPresenter.start(container);
    }
}
