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

package org.eclipse.hudson.maven.plugin.ui.gwt.configure.workspace.internal;

import com.google.gwt.user.client.ui.HasWidgets;

import org.eclipse.hudson.maven.plugin.ui.gwt.configure.workspace.WorkspaceManagerPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.workspace.WorkspaceManagerView;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.workspace.WorkspacePresenter;
import org.sonatype.inject.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link WorkspaceManagerPresenter}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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

    public WorkspaceManagerView getView() {
        return view;
    }

    public void start(final HasWidgets container) {
        checkNotNull(container);
        container.clear();
        container.add(view.asWidget());

        if (defaultWorkspace != null) {
            view.add(defaultWorkspace.getView());
        }
    }
}
