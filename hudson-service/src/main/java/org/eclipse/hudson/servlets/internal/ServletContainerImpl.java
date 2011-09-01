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

package org.eclipse.hudson.servlets.internal;

import org.eclipse.hudson.servlets.ServletContainer;
import org.eclipse.hudson.servlets.ServletContainerAware;
import org.eclipse.hudson.servlets.ServletRegistration;

import hudson.util.PluginServletFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link ServletContainer} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class ServletContainerImpl
    implements ServletContainer
{
    private static final Logger log = LoggerFactory.getLogger(ServletContainerImpl.class);

    private final Map<ServletRegistration, Filter> registrations = new LinkedHashMap<ServletRegistration, Filter>();

    private final List<ServletContainerAware> concerned;

    @Inject
    public ServletContainerImpl(final List<ServletContainerAware> concerned) {
        this.concerned = checkNotNull(concerned);
    }

    private ServletContainerAware[] getConcerned() {
        return concerned.toArray(new ServletContainerAware[concerned.size()]);
    }

    public void start() throws Exception {
        log.debug("Starting");

        // When a ServletContainerAware ext gets executed, we need to set the TCL so that it loads properly, else crazy things will happen
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();

        for (ServletContainerAware target : getConcerned()) {
            Thread.currentThread().setContextClassLoader(target.getClass().getClassLoader());
            try {
                target.setServletContainer(this);
            }
            catch (Exception e) {
                log.error("Exception while exposing servlet container to: " + target, e);
            }
            finally {
                Thread.currentThread().setContextClassLoader(cl);
            }
        }
    }

    public void stop() throws Exception {
        log.debug("Stopping");

        for (Filter filter : registrations.values()) {
            PluginServletFilter.removeFilter(filter);
        }

        registrations.clear();
    }

    public ServletRegistration.Handle register(ServletRegistration registration) throws Exception {
        checkNotNull(registration);

        log.debug("Registering: {}", registration);
        registration = registration.clone();

        ServletRegistrationFilterAdapter filter = new ServletRegistrationFilterAdapter(registration);
        PluginServletFilter.addFilter(filter);

        registrations.put(registration, filter);

        return new HandleImpl(registration, filter);
    }

    private class HandleImpl
        implements ServletRegistration.Handle
    {
        private final ServletRegistration registration;

        private final ServletRegistrationFilterAdapter filterAdapter;

        private HandleImpl(final ServletRegistration registration, final ServletRegistrationFilterAdapter filterAdapter) {
            this.registration = checkNotNull(registration);
            this.filterAdapter = checkNotNull(filterAdapter);
        }

        public ServletRegistration getRegistration() {
            return registration;
        }

        public void setEnabled(final boolean enabled) {
            filterAdapter.setEnabled(enabled);
        }

        public boolean isEnabled() {
            return filterAdapter.isEnabled();
        }
    }
}
