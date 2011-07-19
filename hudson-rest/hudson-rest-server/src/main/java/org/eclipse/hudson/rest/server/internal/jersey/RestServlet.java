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

package org.eclipse.hudson.rest.server.internal.jersey;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.ContainerNotifier;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;
import com.sun.jersey.spi.container.servlet.WebServletConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.hudson.rest.common.Constants.HUDSON_HEADER;
import static org.eclipse.hudson.utils.common.Varargs.$;

/**
 * Augmented Jersey REST servlet.
 *
 * This adds some additional logging and sets up the <tt>X-Hudson</tt> header, and provides Sisu injection support.
 *
 * @author <a href="mailto:jjfarcand@apache.org">Jeanfrancois Arcand</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class RestServlet
    extends ServletContainer
    implements ContainerNotifier
{
    private static final Logger log = LoggerFactory.getLogger(RestServlet.class);

    private static final long serialVersionUID = 1L;

    private final List<ContainerListener> listeners = new ArrayList<ContainerListener>();

    private final IoCComponentProviderFactory componentProviderFactory;

    private ResourceConfig resourceConfig;

    public RestServlet(final IoCComponentProviderFactory componentProviderFactory) {
        this.componentProviderFactory = checkNotNull(componentProviderFactory);
    }

    @Override
    protected ResourceConfig getDefaultResourceConfig(final Map<String, Object> props, final WebConfig webConfig) throws ServletException {
        DefaultResourceConfig config = new DefaultResourceConfig();
        // Need to attach a dummy resource, as Jersey will fail to initialize if there are no root resources configured
        config.getSingletons().add(new DummyResource());
        return config;
    }

    /**
     * Dummy root resource to configure Jersey with so that it can initialize, it needs at least one root resource or it will puke.
     */
    @Path("/internal/dummy-root-resource-for-jersey")
    private static class DummyResource
    {
        // empty
    }

    protected void initiate(final ResourceConfig config, final WebApplication webApp) {
        this.resourceConfig = checkNotNull(config);
        webApp.initiate(config, componentProviderFactory);
    }

    @Override
    public void init() throws ServletException {
        init(new WebServletConfig(this)
        {
            @Override
            public ResourceConfig getDefaultResourceConfig(final Map<String, Object> props) throws ServletException {
                checkNotNull(props);
                props.put(ResourceConfig.PROPERTY_CONTAINER_NOTIFIER, RestServlet.this);
                return super.getDefaultResourceConfig(props);
            }
        });
    }

    @Override
    public void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        assert request != null;
        assert response != null;

        // Log the request URI+URL muck
        String uri = request.getRequestURI();
        if (request.getQueryString() != null) {
            uri = String.format("%s?%s", uri, request.getQueryString());
        }

        if (log.isDebugEnabled()) {
            log.debug("Processing: {} {} ({}) [{}]", $(request.getMethod(), uri, request.getRequestURL(), request.getHeader(HUDSON_HEADER)));
        }

        MDC.put(getClass().getName(), uri);
        try {
            super.service(request, response);
        }
        finally {
            MDC.remove(getClass().getName());
        }

        // Include the version details of the api + model
        response.addHeader(HUDSON_HEADER, String.format("api=%s", getApiVersion()));
    }

    private String getApiVersion() {
        return org.eclipse.hudson.rest.api.Version.get().getVersion();
    }

    public synchronized void addApplication(final Application application) {
        checkNotNull(application);
        checkState(resourceConfig != null);
        resourceConfig.add(application);
        for (ContainerListener listener : listeners) {
            listener.onReload();
        }
    }

    public synchronized void addListener(final ContainerListener listener) {
        checkNotNull(listener);
        listeners.add(listener);
    }
}
