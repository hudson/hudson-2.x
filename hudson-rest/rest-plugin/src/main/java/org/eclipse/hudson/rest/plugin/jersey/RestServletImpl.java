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

import org.eclipse.hudson.rest.server.internal.jersey.RestServlet;

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
