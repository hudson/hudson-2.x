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

import org.acegisecurity.ui.rememberme.TokenBasedRememberMeServices;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.Authentication;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * {@link TokenBasedRememberMeServices} with modification so as not to rely
 * on the user password being available.
 *
 * <p>
 * This allows remember-me to work with security realms where the password
 * is never available in clear text.
 *
 * @author Kohsuke Kawaguchi
 */
public class TokenBasedRememberMeServices2 extends TokenBasedRememberMeServices {
    @Override
    protected String makeTokenSignature(long tokenExpiryTime, UserDetails userDetails) {
        String expectedTokenSignature = DigestUtils.md5Hex(userDetails.getUsername() + ":" + tokenExpiryTime + ":"
                + "N/A" + ":" + getKey());
        return expectedTokenSignature;
    }

    @Override
    protected String retrievePassword(Authentication successfulAuthentication) {
        return "N/A";
    }
}
