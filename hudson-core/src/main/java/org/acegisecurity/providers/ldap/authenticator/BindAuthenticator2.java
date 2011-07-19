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

package org.acegisecurity.providers.ldap.authenticator;

import org.acegisecurity.ldap.InitialDirContextFactory;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * {@link BindAuthenticator} with improved diagnostics.
 * 
 * @author Kohsuke Kawaguchi
 */
public class BindAuthenticator2 extends BindAuthenticator {
    /**
     * If we ever had a successful authentication, 
     */
    private boolean hadSuccessfulAuthentication;

    public BindAuthenticator2(InitialDirContextFactory initialDirContextFactory) {
        super(initialDirContextFactory);
    }

    @Override
    public LdapUserDetails authenticate(String username, String password) {
        LdapUserDetails user = super.authenticate(username, password);
        hadSuccessfulAuthentication = true;
        return user;
    }

    @Override
    void handleBindException(String userDn, String username, Throwable cause) {
        LOGGER.log(hadSuccessfulAuthentication? Level.FINE : Level.WARNING,
            "Failed to bind to LDAP: userDn"+userDn+"  username="+username,cause);
        super.handleBindException(userDn, username, cause);
    }

    private static final Logger LOGGER = Logger.getLogger(BindAuthenticator2.class.getName());
}
