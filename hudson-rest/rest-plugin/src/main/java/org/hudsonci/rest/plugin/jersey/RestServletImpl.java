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

import org.hudsonci.rest.server.internal.jersey.RestServlet;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import hudson.model.Hudson;
import hudson.tasks.Mailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

/**
 * Provides Hudson-specific integration for using {@link RestServlet}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named("jersey")
@Singleton
public class RestServletImpl
    extends RestServlet
{
    private static final Logger log = LoggerFactory.getLogger(RestServletImpl.class);

    @Inject
    public RestServletImpl(final @Named("smoothie") IoCComponentProviderFactory componentProviderFactory) {
        super(componentProviderFactory);
    }

    @Override
    public void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        //
        // TODO: Consider adding a Permission.REST or Permission.API to allow users to be granted/denied access to this channel
        //

        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        if (pathInfo.endsWith("/")) {
            pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
        }

        // Send 400 if not asking for a resource
        if (pathInfo.equals(servletPath)) {
            response.sendError(SC_BAD_REQUEST, "Missing resource location");
        }
        else {
            detectRootUrl(request);
            super.service(request, response);
        }
    }

   /**
    * Make sure that the Hudson root URL is valid, if the URL was not configured (and saved) then its going to be null.
    */
    private void detectRootUrl(final HttpServletRequest request) {
        assert request != null;

        String url = Hudson.getInstance().getRootUrl();
        if (url == null) {
            url = getRootUrlFrom(request);
            log.debug("Determined URL: {}", url);

            // Stuff the value into the Mailer's descriptor, since that is where Hudson looks for it first
            Mailer.descriptor().setHudsonUrl(url);
        }
    }

    private String getRootUrlFrom(final HttpServletRequest request) {
        assert request != null;

        StringBuilder buff = new StringBuilder();
        buff.append(request.getScheme()).append("://");
        buff.append(request.getServerName());
        if (request.getServerPort() != 80) {
            buff.append(':').append(request.getServerPort());
        }
        buff.append(request.getContextPath()).append('/');

        return buff.toString();
    }
}
