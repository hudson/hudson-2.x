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

package org.hudsonci.rest.server.internal.jersey;

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
import static org.hudsonci.utils.common.Varargs.$;
import static org.hudsonci.rest.common.Constants.HUDSON_HEADER;

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
        return org.hudsonci.rest.api.Version.get().getVersion();
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
