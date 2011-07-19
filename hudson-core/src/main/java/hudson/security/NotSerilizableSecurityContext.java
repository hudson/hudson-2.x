
/*******************************************************************************
 *
 * Copyright (c) 2004-2006 Acegi Technology Pty Limited
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kohsuke Kawaguchi   
 *
 *******************************************************************************/ 

package hudson.security;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextImpl;
import org.acegisecurity.Authentication;
import org.acegisecurity.userdetails.UserDetails;

import javax.servlet.http.HttpSession;

/**
 * The same as {@link SecurityContextImpl} but doesn't serialize {@link Authentication}.
 *
 * <p>
 * {@link Authentication} often contains {@link UserDetails} implemented by a plugin,
 * but when it's persisted as a part of {@link HttpSession}, such instance will never
 * de-serialize correctly because the container isn't aware of additional classloading
 * in Hudson.
 *
 * <p>
 * Hudson doesn't work with a clustering anyway, and so it's better to just not persist
 * Authentication at all.
 *
 * See http://www.nabble.com/ActiveDirectory-Plugin%3A-ClassNotFoundException-while-loading--persisted-sessions%3A-td22085140.html
 * for the problem report.
 *
 * @author Kohsuke Kawaguchi
 * @see HttpSessionContextIntegrationFilter2
 */
public class NotSerilizableSecurityContext implements SecurityContext {
    private transient Authentication authentication;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SecurityContextImpl) {
            SecurityContextImpl test = (SecurityContextImpl) obj;

            if ((this.getAuthentication() == null) && (test.getAuthentication() == null)) {
                return true;
            }

            if ((this.getAuthentication() != null) && (test.getAuthentication() != null)
                && this.getAuthentication().equals(test.getAuthentication())) {
                return true;
            }
        }

        return false;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public int hashCode() {
        if (this.authentication == null) {
            return -1;
        } else {
            return this.authentication.hashCode();
        }
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());

        if (this.authentication == null) {
            sb.append(": Null authentication");
        } else {
            sb.append(": Authentication: ").append(this.authentication);
        }

        return sb.toString();
    }
}
