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

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;
import java.util.ArrayList;

import hudson.model.Hudson;

/**
 * {@link Authentication} implementation for {@link Principal}
 * given through {@link HttpServletRequest}.
 *
 * <p>
 * This is used to plug the container authentication to Acegi,
 * for backward compatibility with Hudson &lt; 1.160.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ContainerAuthentication implements Authentication {
    private final Principal principal;
    private GrantedAuthority[] authorities;

    /**
     * Servlet container can tie a {@link ServletRequest} to the request handling thread,
     * so we need to capture all the information upfront to allow {@link Authentication}
     * to be passed to other threads, like update center does. See HUDSON-5382. 
     */
    public ContainerAuthentication(HttpServletRequest request) {
        this.principal = request.getUserPrincipal();
        if (principal==null)
            throw new IllegalStateException(); // for anonymous users, we just don't call SecurityContextHolder.getContext().setAuthentication.   

        // Servlet API doesn't provide a way to list up all roles the current user
        // has, so we need to ask AuthorizationStrategy what roles it is going to check against.
        List<GrantedAuthority> l = new ArrayList<GrantedAuthority>();
        for( String g : Hudson.getInstance().getAuthorizationStrategy().getGroups()) {
            if(request.isUserInRole(g))
                l.add(new GrantedAuthorityImpl(g));
        }
        l.add(SecurityRealm.AUTHENTICATED_AUTHORITY);
        authorities = l.toArray(new GrantedAuthority[l.size()]);
    }

    public GrantedAuthority[] getAuthorities() {
        return authorities;
    }

    public Object getCredentials() {
        return null;
    }

    public Object getDetails() {
        return null;
    }

    public String getPrincipal() {
        return principal.getName();
    }

    public boolean isAuthenticated() {
        return true;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        // noop
    }

    public String getName() {
        return getPrincipal();
    }
}
