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

package org.eclipse.hudson.servlets;

import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Servlet} registration details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ServletRegistration
    implements Cloneable
{
    private String name;

    private Class<? extends Servlet> servletType;

    private Servlet servlet;

    private String uriPrefix;

    private Map<String, String> parameters;

    public String getName() {
        return name;
    }

    public ServletRegistration setName(final String name) {
        this.name = name;
        return this;
    }

    public Class<? extends Servlet> getServletType() {
        return servletType;
    }

    public ServletRegistration setServletType(final Class<? extends Servlet> type) {
        this.servletType = type;
        return this;
    }

    public Servlet getServlet() {
        return servlet;
    }

    public ServletRegistration setServlet(final Servlet servlet) {
        this.servlet = servlet;
        return this;
    }

    public String getUriPrefix() {
        return uriPrefix;
    }

    public ServletRegistration setUriPrefix(final String path) {
        this.uriPrefix = path;
        return this;
    }

    public Map<String, String> getParameters() {
        if (parameters == null) {
            return new HashMap<String, String>();
        }
        return parameters;
    }

    public ServletRegistration setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public ServletRegistration addParameter(final String name, final String value) {
        getParameters().put(name, value);
        return this;
    }

    @Override
    public ServletRegistration clone() {
        try {
            return (ServletRegistration) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw (InternalError) new InternalError().initCause(e);
        }
    }

    @Override
    public String toString() {
        return "ServletRegistration{" +
            "name='" + name + '\'' +
            ", servletType=" + servletType +
            ", servlet=" + servlet +
            ", uriPrefix='" + uriPrefix + '\'' +
            ", parameters=" + parameters +
            '}';
    }

    public static interface Handle
    {
        ServletRegistration getRegistration();
        
        void setEnabled(boolean enabled);

        boolean isEnabled();
    }
}
