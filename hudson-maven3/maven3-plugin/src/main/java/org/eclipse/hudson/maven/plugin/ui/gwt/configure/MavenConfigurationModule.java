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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import org.eclipse.hudson.gwt.common.LoggingEventBus;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.documents.DocumentMasterPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.configure.workspace.WorkspacePresenter;

import javax.inject.Singleton;


/**
 * Injection configuration for the MavenConfiguration module.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class MavenConfigurationModule
    extends AbstractGinModule
{
    protected void configure() {
        bind(EventBus.class).to(LoggingEventBus.class);
        bind(WorkspacePresenter.class).annotatedWith(Names.named("default")).to(DocumentMasterPresenter.class);
    }

    @SuppressWarnings("unused")
    @Provides
    @Singleton
    private Scheduler provideScheduler() {
        return Scheduler.get();
    }
}
