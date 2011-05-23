/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.configure;

import com.google.gwt.user.client.ui.HasWidgets;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.workspace.WorkspaceManagerPresenter;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Controls the MavenConfiguration module.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Singleton
public class MavenConfigurationController
{
    // FIXME: Not sure this is needed, keeping around for now until we have a better way to glue together modules for plugins and pages and such inside of Matrix

    private final WorkspaceManagerPresenter workspaceManagerPresenter;

    @Inject
    public MavenConfigurationController(final WorkspaceManagerPresenter workspaceManagerPresenter) {
        this.workspaceManagerPresenter = checkNotNull(workspaceManagerPresenter);
    }

    public void start(final HasWidgets container) {
        workspaceManagerPresenter.start(container);
    }
}
