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

package org.hudsonci.servlets;

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
