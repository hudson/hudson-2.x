/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
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

package hudson.security;

import groovy.lang.Binding;
import hudson.EnvVars;
import hudson.Functions;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.Util;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.jna.NativeAccessException;
import hudson.util.jna.NativeUtils;
import hudson.util.spring.BeanBuilder;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.User;
import org.springframework.dao.DataAccessException;
import org.springframework.web.context.WebApplicationContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Set;

/**
 * {@link SecurityRealm} that uses Unix PAM authentication.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.282
 */
public class PAMSecurityRealm extends SecurityRealm {

    public final String serviceName;

    @DataBoundConstructor
    public PAMSecurityRealm(String serviceName) {
        serviceName = Util.fixEmptyAndTrim(serviceName);
        if (serviceName == null) {
            serviceName = "sshd"; // use sshd as the default
        }
        this.serviceName = serviceName;
    }

    public static class PAMAuthenticationProvider implements AuthenticationProvider {

        private String serviceName;

        public PAMAuthenticationProvider(String serviceName) {
            this.serviceName = serviceName;
        }

        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            String username = authentication.getPrincipal().toString();
            String password = authentication.getCredentials().toString();

            try {

                Set<String> grps = NativeUtils.getInstance().pamAuthenticate(serviceName, username, password);
                GrantedAuthority[] groups = new GrantedAuthority[grps.size()];
                int i = 0;
                for (String g : grps) {
                    groups[i++] = new GrantedAuthorityImpl(g);
                }
                EnvVars.setHudsonUserEnvVar(username);
                // I never understood why Acegi insists on keeping the password...
                return new UsernamePasswordAuthenticationToken(username, password, groups);
            } catch (NativeAccessException exc) {
                throw new BadCredentialsException(exc.getMessage(), exc);
            }

        }

        public boolean supports(Class clazz) {
            return true;
        }
    }

    public SecurityComponents createSecurityComponents() {
        Binding binding = new Binding();
        binding.setVariable("instance", this);

        BeanBuilder builder = new BeanBuilder();
        builder.parse(Hudson.getInstance().servletContext.getResourceAsStream("/WEB-INF/security/PAMSecurityRealm.groovy"), binding);
        WebApplicationContext context = builder.createApplicationContext();
        return new SecurityComponents(
                findBean(AuthenticationManager.class, context),
                new UserDetailsService() {

                    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
                        try {
                            if (!NativeUtils.getInstance().checkUnixUser(username)) {
                                throw new UsernameNotFoundException("No such Unix user: " + username);
                            }
                        } catch (NativeAccessException exc) {
                            throw new DataAccessException("Failed to find Unix User", exc) {
                            };
                        }

                        // return some dummy instance
                        return new User(username, "", true, true, true, true,
                                new GrantedAuthority[]{AUTHENTICATED_AUTHORITY});
                    }
                });
    }

    @Override
    public GroupDetails loadGroupByGroupname(final String groupname) throws UsernameNotFoundException, DataAccessException {
        try {
            if (!NativeUtils.getInstance().checkUnixGroup(groupname)) {
                throw new UsernameNotFoundException("No such Unix group: " + groupname);
            }
        } catch (NativeAccessException exc) {
            throw new DataAccessException("Failed to find Unix Group", exc) {
            };
        }

        return new GroupDetails() {

            @Override
            public String getName() {
                return groupname;
            }
        };
    }

    public static final class DescriptorImpl extends Descriptor<SecurityRealm> {

        public String getDisplayName() {
            return Messages.PAMSecurityRealm_DisplayName();
        }

        public FormValidation doTest() {
            try {
                String message = NativeUtils.getInstance().checkPamAuthentication();
                if (message.startsWith("Error:")) {
                    return FormValidation.error(message.replaceFirst("Error:", ""));
                } else {
                    return FormValidation.ok(message);
                }
            } catch (NativeAccessException exc) {
                return FormValidation.error("Native Support for PAM Authentication not available.");
            }
        }
    }

    @Extension
    public static DescriptorImpl install() {
        if (!Functions.isWindows()) {
            return new DescriptorImpl();
        }
        return null;
    }
}
