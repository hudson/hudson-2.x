/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.plugin.ui.gwt.buildinfo.gin;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Provides;
import org.hudsonci.gwt.common.LoggingEventBus;

import javax.inject.Singleton;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.ModuleInfoPresenter.ModuleInfoView;

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
