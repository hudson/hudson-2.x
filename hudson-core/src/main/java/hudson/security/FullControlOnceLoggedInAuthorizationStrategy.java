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
*    Kohsuke Kawaguchi, Seiji Sogabe
 *     
 *
 *******************************************************************************/ 

package hudson.security;

import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.Extension;

import java.util.Collections;
import java.util.List;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

/**
 * {@link AuthorizationStrategy} that grants full-control to authenticated user
 * (other than anonymous users.)
 *
 * @author Kohsuke Kawaguchi
 */
public class FullControlOnceLoggedInAuthorizationStrategy extends AuthorizationStrategy {
    @Override
    public ACL getRootACL() {
        return THE_ACL;
    }

    public List<String> getGroups() {
        return Collections.emptyList();
    }

    private static final SparseACL THE_ACL = new SparseACL(null);

    static {
        THE_ACL.add(ACL.EVERYONE,Hudson.ADMINISTER,true);
        THE_ACL.add(ACL.ANONYMOUS,Hudson.ADMINISTER,false);
        THE_ACL.add(ACL.ANONYMOUS,Permission.READ,true);
    }

    @Extension
    public static final Descriptor<AuthorizationStrategy> DESCRIPTOR = new Descriptor<AuthorizationStrategy>() {
        public String getDisplayName() {
            return Messages.FullControlOnceLoggedInAuthorizationStrategy_DisplayName();
        }

        @Override
        public AuthorizationStrategy newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new FullControlOnceLoggedInAuthorizationStrategy();
        }

        @Override
        public String getHelpFile() {
            return "/help/security/full-control-once-logged-in.html";
        }
    };
}
