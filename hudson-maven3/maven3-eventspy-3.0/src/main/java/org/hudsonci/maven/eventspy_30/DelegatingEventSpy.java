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

package org.hudsonci.maven.eventspy_30;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import org.hudsonci.utils.common.TestAccessible;
import org.apache.maven.BuildAbort;
import org.apache.maven.eventspy.EventSpy;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.hudsonci.maven.eventspy.common.Constants;
import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.inject.BeanEntry;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.hudsonci.maven.eventspy.common.Constants.DELEGATE_PROPERTY;

/**
 * Delegates to a {@link EventSpy} component configured via {@link Constants#DELEGATE_PROPERTY}.
 * This is the main {@link EventSpy} which Maven will load and is configured via Plexus, delegate
 * is loaded via JSR-330.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Component(role=EventSpy.class, hint="delegating")
public class DelegatingEventSpy
    extends EventSpySupport
    implements Module
{
    @Requirement
    private MutableBeanLocator locator;

    private EventSpy delegate;

    /**
     * For Plexus.
     */
    @SuppressWarnings({"unused"})
    public DelegatingEventSpy() {
    }

    @TestAccessible
    public DelegatingEventSpy(final EventSpy delegate) {
        this.delegate = checkNotNull(delegate);
    }

    public EventSpy getDelegate() {
        checkState(delegate != null);
        return delegate;
    }

    public void configure(final Binder binder) {
        binder.bind(MutableBeanLocator.class).toInstance(locator);
    }

    @Override
    public void init(final Context context) throws Exception {
        try {
            log.debug("Initializing w/context: {}", context);
            checkNotNull(context);
            super.init(context);

            // Spit out some trace information
            if (log.isTraceEnabled()) {
                log.trace("Context keys: {}", context.getData().keySet());
                log.trace("Container: {}", getContainer());
                log.trace("Working dir: {}", getWorkingDirectory());

                log.trace("Version properties:");
                for (Map.Entry entry : getVersionProperties().entrySet()) {
                    log.trace("  {}='{}'", entry.getKey(), entry.getValue());
                }

                log.trace("User properties:");
                for (Map.Entry entry : getUserProperties().entrySet()) {
                    log.trace("  {}='{}'", entry.getKey(), entry.getValue());
                }

                log.trace("System properties:");
                for (Map.Entry entry : getSystemProperties().entrySet()) {
                    log.trace("  {}='{}'", entry.getKey(), entry.getValue());
                }
            }

            // Delegate is non-null in testing case
            if (delegate == null) {
                delegate = loadDelegate();
                log.debug("Delegate: {}", delegate);
            }

            getDelegate().init(context);
        }
        catch (Throwable e) {
            log.error("Init failed", e);

            // Abort on failure to init()
            if (e instanceof BuildAbort) {
                throw (BuildAbort)e;
            }
            throw new BuildAbort("Failed to initialize", e);
        }
    }

    // FIXME: May need to provide a separate EventSpyDelegate intf to use here, since once Maven becomes more Sisu-aware for loading components it will also find our alternative spy impls

    private EventSpy loadDelegate() {
        checkState(locator != null);

        // Setup the Injector to load spy impls from
        URL[] scanPath = {
            getClass().getProtectionDomain().getCodeSource().getLocation(),
        };
        URLClassSpace space = new URLClassSpace(getClass().getClassLoader(), scanPath);
        Injector injector = Guice.createInjector(new WireModule(new SpaceModule(space), this));

        // Log what spies we know about
        if (log.isDebugEnabled()) {
            log.debug("Known spies:");
            for (BeanEntry<Annotation, EventSpy> spy : locator.locate(Key.get(EventSpy.class))) {
                log.debug("  {}", spy);
            }
        }

        // Load the delegate, default to RemotingEventSpy
        String name = getProperty(DELEGATE_PROPERTY, RemotingEventSpy.class.getName());
        log.debug("Loading delegate named: {}", name);
        return injector.getInstance(Key.get(EventSpy.class, Names.named(name)));
    }

    @Override
    public void close() throws Exception {
        try {
            log.debug("Closing");
            ensureOpened();
            getDelegate().close();
        }
        catch (Throwable e) {
            log.error("Close failed", e);

            // Abort on failure to close()
            if (e instanceof BuildAbort) {
                throw (BuildAbort)e;
            }
            throw new BuildAbort("Failed to close", e);
        }
    }

    @Override
    public void onEvent(final Object event) throws Exception {
        try {
            ensureOpened();
            getDelegate().onEvent(event);
        }
        catch (Exception e) {
            log.error("Failed to handle event", e);
            // complain but continue
            throw e;
        }
    }
}
