/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.configure.workspace.internal;

import com.google.gwt.user.client.ui.HasWidgets;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.workspace.WorkspaceManagerPresenter;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.workspace.WorkspaceManagerView;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.workspace.WorkspacePresenter;
import org.sonatype.inject.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link WorkspaceManagerPresenter}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Singleton
public class WorkspaceManagerPresenterImpl
    implements WorkspaceManagerPresenter
{
    private final WorkspaceManagerView view;

    private final WorkspacePresenter defaultWorkspace;

    @Inject
    public WorkspaceManagerPresenterImpl(final WorkspaceManagerView view, final @Named("default") @Nullable WorkspacePresenter defaultWorkspace) {
        this.view = checkNotNull(view);
        this.defaultWorkspace = defaultWorkspace;
        view.setPresenter(this);
    }

    @Override
    public WorkspaceManagerView getView() {
        return view;
    }

    @Override
    public void start(final HasWidgets container) {
        checkNotNull(container);
        container.clear();
        container.add(view.asWidget());

        if (defaultWorkspace != null) {
            view.add(defaultWorkspace.getView());
        }
    }
}
