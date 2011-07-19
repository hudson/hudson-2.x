/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Servlet {@link Filter} that chains multiple {@link Filter}s.
 *
 * @author Kohsuke Kawaguchi
 */
public class ChainedServletFilter implements Filter {
    // array is assumed to be immutable once set
    protected volatile Filter[] filters;

    public ChainedServletFilter() {
        filters = new Filter[0];
    }

    public ChainedServletFilter(Filter... filters) {
        this(Arrays.asList(filters));
    }

    public ChainedServletFilter(Collection<? extends Filter> filters) {
        setFilters(filters);
    }

    public void setFilters(Collection<? extends Filter> filters) {
        this.filters = filters.toArray(new Filter[filters.size()]);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        if (LOGGER.isLoggable(Level.FINEST))
            for (Filter f : filters)
                LOGGER.finest("ChainedServletFilter contains: " + f);

        for (Filter f : filters)
            f.init(filterConfig);
    }

    public void doFilter(ServletRequest request, ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        LOGGER.entering(ChainedServletFilter.class.getName(), "doFilter");

        new FilterChain() {
            private int position=0;
            // capture the array for thread-safety
            private final Filter[] filters = ChainedServletFilter.this.filters;

            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                if(position==filters.length) {
                    // reached to the end
                    chain.doFilter(request,response);
                } else {
                    // call next
                    filters[position++].doFilter(request,response,this);
                }
            }
        }.doFilter(request,response);
    }

    public void destroy() {
        for (Filter f : filters)
            f.destroy();
    }

    private static final Logger LOGGER = Logger.getLogger(ChainedServletFilter.class.getName());
}
