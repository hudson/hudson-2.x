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

import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.springframework.web.context.WebApplicationContext;
import org.kohsuke.stapler.StaplerRequest;
import groovy.lang.Binding;
import hudson.model.Descriptor;
import hudson.util.spring.BeanBuilder;
import hudson.Extension;
import net.sf.json.JSONObject;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;

/**
 * {@link SecurityRealm} that accepts {@link ContainerAuthentication} object
 * without any check (that is, by assuming that the such token is
 * already authenticated by the container.)
 * 
 * @author Kohsuke Kawaguchi
 */
public final class LegacySecurityRealm extends SecurityRealm implements AuthenticationManager {
    public SecurityComponents createSecurityComponents() {
        return new SecurityComponents(this);
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if(authentication instanceof ContainerAuthentication)
            return authentication;
        else
            return null;
    }

    /**
     * To have the username/password authenticated by the container,
     * submit the form to the URL defined by the servlet spec.
     */
    @Override
    public String getAuthenticationGatewayUrl() {
        return "j_security_check";
    }

    @Override
    public String getLoginUrl() {
        return "loginEntry";
    }

    /**
     * Filter to run for the LegacySecurityRealm is the
     * ChainServletFilter legacy from /WEB-INF/security/SecurityFilters.groovy.
     */
    @Override
    public Filter createFilter(FilterConfig filterConfig) {
        Binding binding = new Binding();
        SecurityComponents sc = this.createSecurityComponents();
        binding.setVariable("securityComponents", sc);
        binding.setVariable("securityRealm",this);
        BeanBuilder builder = new BeanBuilder();
        builder.parse(filterConfig.getServletContext().getResourceAsStream("/WEB-INF/security/SecurityFilters.groovy"),binding);
        
        WebApplicationContext context = builder.createApplicationContext();
        
        return (Filter) context.getBean("legacy");
    }

    @Extension
    public static final Descriptor<SecurityRealm> DESCRIPTOR = new Descriptor<SecurityRealm>() {
        public SecurityRealm newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new LegacySecurityRealm();
        }

        public String getDisplayName() {
            return Messages.LegacySecurityRealm_Displayname();
        }

        public String getHelpFile() {
            return "/help/security/container-realm.html";
        }
    };
}
