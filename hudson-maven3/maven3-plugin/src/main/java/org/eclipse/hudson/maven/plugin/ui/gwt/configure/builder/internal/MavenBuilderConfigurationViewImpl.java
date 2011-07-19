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

package org.eclipse.hudson.maven.plugin.ui.gwt.configure.builder.internal;

import com.google.gwt.user.client.ui.ResizeComposite;

import javax.inject.Inject;

import org.eclipse.hudson.maven.plugin.ui.gwt.configure.builder.MavenBuilderConfigurationPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.builder.MavenBuilderConfigurationView;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class MavenBuilderConfigurationViewImpl
    extends ResizeComposite
    implements MavenBuilderConfigurationView
{
    private MavenBuilderConfigurationPresenter presenter;

    @Inject
    public MavenBuilderConfigurationViewImpl() {
        ensureDebugId("maven-builder-configuration-view");
        // TODO:
    }

    public void setPresenter(final MavenBuilderConfigurationPresenter presenter) {
        this.presenter = checkNotNull(presenter);
    }
}
