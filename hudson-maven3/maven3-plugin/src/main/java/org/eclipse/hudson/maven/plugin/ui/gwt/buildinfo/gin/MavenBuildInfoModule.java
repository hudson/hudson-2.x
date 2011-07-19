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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.gin;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Provides;

import org.eclipse.hudson.gwt.common.LoggingEventBus;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.ModuleInfoPresenter.ModuleInfoView;

import javax.inject.Singleton;


/**
 * Configuration for GIN dependency injection.
 *
 * Providers are private to indicate that these methods should not be called from Java code.  Objects should be configured for injection or add a
 * factory method to {@link MavenBuildInfoInjector} or another {@link Ginjector} to retrieve objects from GIN.
 *
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@SuppressWarnings("unused")
public class MavenBuildInfoModule
    extends AbstractGinModule
{
    @Override
    protected void configure() {
        bind(EventBus.class).to(LoggingEventBus.class);
        bind(IsWidget.class).annotatedWith(FirstShownInfoDisplay.class).to(ModuleInfoView.class);
    }

    @Provides
    @Singleton
    private Scheduler provideScheduler() {
        return Scheduler.get();
    }
}
