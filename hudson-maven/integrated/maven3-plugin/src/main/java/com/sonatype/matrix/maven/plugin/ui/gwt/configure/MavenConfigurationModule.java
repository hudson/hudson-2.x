/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.configure;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.sonatype.matrix.gwt.common.LoggingEventBus;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.documents.DocumentMasterPresenter;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.workspace.WorkspacePresenter;

import javax.inject.Singleton;

/**
 * Injection configuration for the MavenConfiguration module.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public class MavenConfigurationModule
    extends AbstractGinModule
{
    @Override
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
