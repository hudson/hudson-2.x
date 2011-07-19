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

package org.eclipse.hudson.rest.plugin.jersey;

import org.eclipse.hudson.rest.plugin.ApiProvider;
import org.eclipse.hudson.rest.plugin.RestComponentProvider;
import org.eclipse.hudson.rest.server.internal.jersey.RestServlet;
import org.eclipse.hudson.servlets.ServletContainer;
import org.eclipse.hudson.servlets.ServletContainerAware;
import org.eclipse.hudson.servlets.ServletRegistration;
import org.eclipse.hudson.utils.id.OID;

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
import static org.eclipse.hudson.rest.common.Constants.BASE_REST_PATH;

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
