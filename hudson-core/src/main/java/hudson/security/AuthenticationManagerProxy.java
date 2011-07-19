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

import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.DisabledException;

/**
 * {@link AuthenticationManager} proxy that delegates to another instance.
 *
 * <p>
 * This is used so that we can set up servlet filters first (which requires a reference
 * to {@link AuthenticationManager}), then later change the actual authentication manager
 * (and its set up) at runtime.
 *
 * @author Kohsuke Kawaguchi
 */
public class AuthenticationManagerProxy implements AuthenticationManager {
    private volatile AuthenticationManager delegate;

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AuthenticationManager m = delegate; // fix the reference we are working with

        if(m ==null)
            throw new DisabledException("Authentication service is still not ready yet");
        else
            return m.authenticate(authentication);
    }

    public void setDelegate(AuthenticationManager manager) {
        this.delegate = manager;
    }
}
