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

package org.hudsonci.servlets.internal;

import org.hudsonci.servlets.ServletContainer;
import org.hudsonci.servlets.ServletContainerAware;
import org.hudsonci.servlets.ServletRegistration;
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

        // When a ServletContainerAware ext gets executed, we need to set the TCL so that it loads properly, else crazy shit will happen
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
