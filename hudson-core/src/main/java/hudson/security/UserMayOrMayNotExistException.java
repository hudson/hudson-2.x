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

import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.userdetails.UserDetailsService;

/**
 * Thrown from {@link UserDetailsService#loadUserByUsername(String)}
 * to indicate that the underlying {@link SecurityRealm} is incapable
 * of retrieving the information, and furthermore, the system cannot
 * tell if such an user exists or not.
 *
 * <p>
 * This happens, for example, when the security realm is on top of the servlet implementation,
 * there's no way of even knowing if an user of a given name exists or not.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.280
 */
public class UserMayOrMayNotExistException extends UsernameNotFoundException {
    public UserMayOrMayNotExistException(String msg) {
        super(msg);
    }

    public UserMayOrMayNotExistException(String msg, Object extraInformation) {
        super(msg, extraInformation);
    }

    public UserMayOrMayNotExistException(String msg, Throwable t) {
        super(msg, t);
    }
}
