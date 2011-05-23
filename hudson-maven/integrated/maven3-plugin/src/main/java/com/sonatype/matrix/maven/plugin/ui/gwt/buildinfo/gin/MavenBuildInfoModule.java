/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.gin;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Provides;
import com.sonatype.matrix.gwt.common.LoggingEventBus;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.ModuleInfoPresenter.ModuleInfoView;

import javax.inject.Singleton;

/**
 * Configuration for GIN dependency injection.
 *
 * Providers are private to indicate that these methods should not be called from Java code.  Objects should be configured for injection or add a
 * factory method to {@link MavenBuildInfoInjector} or another {@link Ginjector} to retrieve objects from GIN.
 *
 * @author Jamie Whitehouse
 * @since 1.1
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
