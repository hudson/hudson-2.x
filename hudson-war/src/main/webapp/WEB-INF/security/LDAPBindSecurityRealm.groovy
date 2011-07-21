/**************************************************************************
#
# Copyright (C) 2004-2010 Oracle Corporation
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#         Kohsuke Kawaguchi
#
#**************************************************************************/ 

import org.acegisecurity.providers.ProviderManager
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationProvider
import org.acegisecurity.providers.ldap.LdapAuthenticationProvider
import org.acegisecurity.providers.ldap.authenticator.BindAuthenticator2
import org.acegisecurity.ldap.DefaultInitialDirContextFactory
import org.acegisecurity.ldap.search.FilterBasedLdapUserSearch
import org.acegisecurity.providers.rememberme.RememberMeAuthenticationProvider
import hudson.model.Hudson
import hudson.security.LDAPSecurityRealm.AuthoritiesPopulatorImpl
import hudson.Util
import javax.naming.Context

/*
    Configure LDAP as the authentication realm.

    Authentication is performed by doing LDAP bind.
    The 'instance' object refers to the instance of LDAPSecurityRealm
*/

initialDirContextFactory(DefaultInitialDirContextFactory, instance.getLDAPURL() ) {
  if(instance.managerDN!=null) {
    managerDn = instance.managerDN;
    managerPassword = instance.getManagerPassword();
  }
  extraEnvVars = [(Context.REFERRAL):"follow"];
}

ldapUserSearch(FilterBasedLdapUserSearch, instance.userSearchBase, instance.userSearch, initialDirContextFactory) {
    searchSubtree=true
}

bindAuthenticator(BindAuthenticator2,initialDirContextFactory) {
    // this is when you the user name can be translated into DN.
//  userDnPatterns = [
//    "uid={0},ou=people"
//  ]
    // this is when we need to find it.
    userSearch = ldapUserSearch;
}

authoritiesPopulator(AuthoritiesPopulatorImpl, initialDirContextFactory, instance.groupSearchBase) {
    // see DefaultLdapAuthoritiesPopulator for other possible configurations
    searchSubtree = true;
    groupSearchFilter = "(| (member={0}) (uniqueMember={0}) (memberUid={1}))";
    // rolePrefix = "ROLE_"; // Default is "ROLE_"
    // convertToUpperCase = false; // Default is true
}

authenticationManager(ProviderManager) {
    providers = [
        // talk to LDAP
        bean(LdapAuthenticationProvider,bindAuthenticator,authoritiesPopulator),

    // these providers apply everywhere
        bean(RememberMeAuthenticationProvider) {
            key = Hudson.getInstance().getSecretKey();
        },
        // this doesn't mean we allow anonymous access.
        // we just authenticate anonymous users as such,
        // so that later authorization can reject them if so configured
        bean(AnonymousAuthenticationProvider) {
            key = "anonymous"
        }
    ]
}
