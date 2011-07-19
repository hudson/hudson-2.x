/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.diagnosis;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.util.FormValidation;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.WebMethod;

import java.io.IOException;

/**
 * Looks out for a broken reverse proxy setup that doesn't rewrite the location header correctly.
 *
 * <p>
 * Have the JavaScript make an AJAX call, to which we respond with 302 redirect. If the reverse proxy
 * is done correctly, this will be handled by {@link #doFoo()}, but otherwise we'll report that as an error.
 * Unfortunately, {@code XmlHttpRequest} doesn't expose properties that allow the client-side JavaScript
 * to learn the details of the failure, so we have to make do with limited information.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class ReverseProxySetupMonitor extends AdministrativeMonitor {
    @Override
    public boolean isActivated() {
        // return true to always inject an HTML fragment to perform a test
        return true;
    }

    public HttpResponse doTest() {
        return new HttpRedirect("test-for-reverse-proxy-setup");
    }

    @WebMethod(name="test-for-reverse-proxy-setup")
    public FormValidation doFoo() {
        return FormValidation.ok();
    }

    /**
     * Depending on whether the user said "yes" or "no", send him to the right place.
     */
    public HttpResponse doAct(@QueryParameter String no) throws IOException {
        if(no!=null) { // dismiss
            disable(true);
            // of course the irony is that this redirect won't work
            return HttpResponses.redirectViaContextPath("/manage");
        } else {
            return new HttpRedirect("http://wiki.hudson-ci.org/display/HUDSON/Running+Hudson+behind+Apache#RunningHudsonbehindApache-modproxywithHTTPS");
        }
    }
}

