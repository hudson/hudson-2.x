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

package hudson.model;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import hudson.Util;
import hudson.security.ACL;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import org.acegisecurity.AccessDeniedException;

/**
 * Authorization token to allow projects to trigger themselves under the secured environment.
 *
 * @author Kohsuke Kawaguchi
 * @see BuildableItem
 * @deprecated 2008-07-20
 *      Use {@link ACL} and {@link AbstractProject#BUILD}. This code is only here
 *      for the backward compatibility.
 */
public final class BuildAuthorizationToken {
    private final String token;

    public BuildAuthorizationToken(String token) {
        this.token = token;
    }

    public static BuildAuthorizationToken create(StaplerRequest req) {
        if (req.getParameter("pseudoRemoteTrigger") != null) {
            String token = Util.fixEmpty(req.getParameter("authToken"));
            if(token!=null)
                return new BuildAuthorizationToken(token);
        }
        
        return null;
    }

    public static void checkPermission(AbstractProject project, BuildAuthorizationToken token, StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (!Hudson.getInstance().isUseSecurity())
            return;    // everyone is authorized

        if(token!=null && token.token != null) {
            //check the provided token
            String providedToken = req.getParameter("token");
            if (providedToken != null && providedToken.equals(token.token))
                return;
            if (providedToken != null)
                throw new AccessDeniedException(Messages.BuildAuthorizationToken_InvalidTokenProvided());
        }

        project.checkPermission(AbstractProject.BUILD);
    }

    public String getToken() {
        return token;
    }

    public static final class ConverterImpl extends AbstractSingleValueConverter {
        public boolean canConvert(Class type) {
            return type== BuildAuthorizationToken.class;
        }

        public Object fromString(String str) {
            return new BuildAuthorizationToken(str);
        }

        @Override
        public String toString(Object obj) {
            return ((BuildAuthorizationToken)obj).token;
        }
    }
}
