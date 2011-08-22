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

import org.eclipse.hudson.servlets.ServletRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wraps a {@link Servlet} as a {@link Filter} for installation via {@link hudson.util.PluginServletFilter}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ServletRegistrationFilterAdapter
    implements Filter
{
    private static final Logger log = LoggerFactory.getLogger(ServletRegistrationFilterAdapter.class);

    private final ServletRegistration registration;

    private final Servlet servlet;

    private final String uriPrefix;

    private boolean enabled;

    public ServletRegistrationFilterAdapter(final ServletRegistration registration) throws Exception {
        this.registration = checkNotNull(registration);
        this.servlet = createServlet();

        if (registration.getName() == null) {
            registration.setName(servlet.getClass().getName());
        }

        uriPrefix = registration.getUriPrefix();
        if (uriPrefix == null) {
            throw new IllegalArgumentException("Registration missing uriPrefix");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    private Servlet createServlet() throws Exception {
        Servlet servlet = registration.getServlet();
        if (servlet != null) {
            return servlet;
        }

        Class<? extends Servlet> type = registration.getServletType();
        if (type != null) {
            return type.newInstance();
        }

        throw new IllegalArgumentException("Registration missing servlet or servlet type");
    }

    public void init(final FilterConfig config) throws ServletException {
        checkNotNull(config);

        servlet.init(new ServletConfig()
        {
            public String getServletName() {
                return registration.getName();
            }

            public ServletContext getServletContext() {
                return config.getServletContext();
            }

            public Enumeration getInitParameterNames() {
                final Iterator<String> iter = registration.getParameters().keySet().iterator();

                return new Enumeration()
                {
                    public boolean hasMoreElements() {
                        return iter.hasNext();
                    }

                    public Object nextElement() {
                        return iter.next();
                    }
                };
            }

            public String getInitParameter(final String name) {
                return registration.getParameters().get(name);
            }
        });
    }

    public void destroy() {
        servlet.destroy();
    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException
    {
        assert chain != null;

        if (isEnabled() && request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
        }
        else {
            chain.doFilter(request, response);
        }
    }

    private void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
        throws IOException, ServletException
    {
        assert request != null;
        assert response != null;
        assert chain != null;

        String contextPath = request.getContextPath();
        if (!contextPath.endsWith("/") && !uriPrefix.startsWith("/")) {
            contextPath = contextPath + '/';
        }

        if (request.getRequestURI().startsWith(contextPath + uriPrefix)) {
            // Wrap the request to augment the servlet uriPrefix
            HttpServletRequestWrapper req = new HttpServletRequestWrapper(request)
            {
                @Override
                public String getServletPath() {
                    return String.format("/%s", uriPrefix);
                }
            };

            servlet.service(req, response);
        }
        else {
            chain.doFilter(request, response);
        }
    }
}
