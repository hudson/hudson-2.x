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

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;

/**
 * {@link UserDetailsService} proxy that delegates to another instance.
 * 
 * @author Kohsuke Kawaguchi
 */
public class UserDetailsServiceProxy implements UserDetailsService {
    private volatile UserDetailsService delegate;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        UserDetailsService uds = delegate;  // fix the reference for concurrency support

        if(uds ==null)
            throw new UserMayOrMayNotExistException(Messages.UserDetailsServiceProxy_UnableToQuery(username));
        return uds.loadUserByUsername(username);
    }

    public void setDelegate(UserDetailsService core) {
        this.delegate = core;
    }

}
