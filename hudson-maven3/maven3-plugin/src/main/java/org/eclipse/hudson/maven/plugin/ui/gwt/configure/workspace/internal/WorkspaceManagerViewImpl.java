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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.TabLayoutPanel;

import javax.inject.Singleton;

import org.eclipse.hudson.maven.plugin.ui.gwt.configure.workspace.WorkspaceManagerPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.workspace.WorkspaceManagerView;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.workspace.WorkspaceView;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link WorkspaceManagerView}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Singleton
public class WorkspaceManagerViewImpl
    extends ResizeComposite
    implements WorkspaceManagerView
{
    private final TabLayoutPanel tabPanel;

    private WorkspaceManagerPresenter presenter;

    public WorkspaceManagerViewImpl() {
        tabPanel = new TabLayoutPanel(2.0, Unit.EM);
        tabPanel.setSize("100%", "100%");
        initWidget(tabPanel);
        ensureDebugId("workspace-manager-view");
    }

    public void setPresenter(final WorkspaceManagerPresenter presenter) {
        this.presenter = checkNotNull(presenter);
    }

    public void add(final WorkspaceView view) {
        tabPanel.add(view.asWidget(), view.getWorkspaceTitle());
    }
}
