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
package hudson.security

import org.jvnet.hudson.test.HudsonTestCase
import hudson.security.LDAPSecurityRealm.LDAPUserDetailsService
import org.acegisecurity.ldap.LdapUserSearch
import org.acegisecurity.userdetails.ldap.LdapUserDetailsImpl
import javax.naming.directory.BasicAttributes
import org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator
import org.acegisecurity.GrantedAuthority

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class LDAPSecurityRealmTest extends HudsonTestCase {
    /**
     * This minimal test still causes the 'LDAPBindSecurityRealm.groovy' to be parsed, allowing us to catch
     * basic syntax errors and such.
     */
    void testGroovyBeanDef() {
        hudson.securityRealm = new LDAPSecurityRealm("ldap.itd.umich.edu",null,null,null,null,null,null);
        println hudson.securityRealm.securityComponents // force the component creation
    }

    void testSessionStressTest() {
        LDAPUserDetailsService s = new LDAPUserDetailsService(
                { username ->
                    def e = new LdapUserDetailsImpl.Essence();
                    e.username = username;
                    def ba = new BasicAttributes()
                    ba.put("test",username);
                    ba.put("xyz","def");
                    e.attributes = ba;
                    return e.createUserDetails();
                } as LdapUserSearch,
                { details -> new GrantedAuthority[0] } as LdapAuthoritiesPopulator);
        def d1 = s.loadUserByUsername("me");
        def d2 = s.loadUserByUsername("you");
        def d3 = s.loadUserByUsername("me");
        // caching should reuse the same attributes
        assertSame(d1.attributes,d3.attributes);
        assertNotSame(d1.attributes,d2.attributes);
    }
}
