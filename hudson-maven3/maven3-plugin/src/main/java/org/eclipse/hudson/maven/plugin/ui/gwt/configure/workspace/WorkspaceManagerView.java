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

package org.eclipse.hudson.maven.plugin.ui.gwt.configure.workspace;

import org.eclipse.hudson.maven.plugin.ui.gwt.configure.workspace.internal.WorkspaceManagerViewImpl;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

/**
 * Provides the workspace UI.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@ImplementedBy(WorkspaceManagerViewImpl.class)
public interface WorkspaceManagerView
    extends IsWidget
{
    void setPresenter(WorkspaceManagerPresenter presenter);

    void add(WorkspaceView view);
}
