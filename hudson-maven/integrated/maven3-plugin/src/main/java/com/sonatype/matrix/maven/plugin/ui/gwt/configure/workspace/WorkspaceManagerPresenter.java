/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.configure.workspace;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.workspace.internal.WorkspaceManagerPresenterImpl;

/**
 * Manages the workspace UI.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@ImplementedBy(WorkspaceManagerPresenterImpl.class)
public interface WorkspaceManagerPresenter
{
    WorkspaceManagerView getView();

    void start(HasWidgets container);
}
