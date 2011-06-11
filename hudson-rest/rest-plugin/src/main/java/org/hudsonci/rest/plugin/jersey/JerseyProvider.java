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

package org.hudsonci.rest.plugin.jersey;

import org.hudsonci.utils.id.OID;
import org.hudsonci.rest.server.internal.jersey.RestServlet;
import org.hudsonci.servlets.ServletContainer;
import org.hudsonci.servlets.ServletContainerAware;
import org.hudsonci.servlets.ServletRegistration;

import org.hudsonci.rest.plugin.ApiProvider;
import org.hudsonci.rest.plugin.RestComponentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.hudsonci.rest.common.Constants.BASE_REST_PATH;

/**
 * JAX-RS API {@link ApiProvider} backed by Jersey.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:jjfarcand@apache.org">Jeanfrancois Arcand</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class JerseyProvider
    extends ApiProvider
{
    private static final Logger log = LoggerFactory.getLogger(JerseyProvider.class);

    private static ServletRegistration.Handle handle;

    public static class ProviderApplication
        extends Application
    {
        private final RestComponentProvider provider;

        public ProviderApplication(final RestComponentProvider provider) {
            this.provider = checkNotNull(provider);
        }

        public RestComponentProvider getProvider() {
            return provider;
        }

        @Override
        public Set<Class<?>> getClasses() {
            Set<Class<?>> set = new HashSet<Class<?>>();
            set.addAll(Arrays.asList(getProvider().getClasses()));
            return set;
        }

        @Override
        public Set<Object> getSingletons() {
            Set<Object> set = new HashSet<Object>();
            set.addAll(Arrays.asList(getProvider().getSingletons()));
            return set;
        }
    }

    @Override
    public boolean isEnabled() {
        return handle != null && handle.isEnabled();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        if (handle != null) {
            handle.setEnabled(enabled);
        }
    }

    @Override
    public String toString() {
        return "JAX-RS (Jersey)";
    }

    @Named
    @Singleton
    public static class ServletInstaller
        implements ServletContainerAware
    {
        private final RestServlet servlet;

        private final List<RestComponentProvider> providers;

        @Inject
        public ServletInstaller(final @Named("jersey") RestServlet servlet, final List<RestComponentProvider> providers) {
            this.servlet = checkNotNull(servlet);
            this.providers = checkNotNull(providers);
        }

        public void setServletContainer(final ServletContainer container) throws Exception {
            assert container != null;

            log.debug("Installing Jersey servlet");

            ServletRegistration reg = new ServletRegistration();
            reg.setServlet(servlet);
            reg.setUriPrefix(BASE_REST_PATH);
            handle = container.register(reg);

            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            
            // Add an application for each registered provider
            for (RestComponentProvider provider : providers) {
                Thread.currentThread().setContextClassLoader(provider.getClass().getClassLoader());
                try {
                    includeComponents(provider);
                }
                finally {
                    Thread.currentThread().setContextClassLoader(cl);
                }
            }
        }

        private void includeComponents(final RestComponentProvider provider) {
            assert provider != null;

            if (log.isDebugEnabled()) {
                log.debug("Adding application for provider: {}", provider);

                for (Class type : provider.getClasses()) {
                    log.debug("  {}", type.getName());
                }
                for (Object obj : provider.getSingletons()) {
                    log.debug("  {}", OID.render(obj));
                }
            }

            addApplication(new ProviderApplication(provider));
        }

        private void addApplication(final Application app) {
            checkState(servlet != null);
            servlet.addApplication(app);
        }
    }
}
