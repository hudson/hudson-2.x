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
*    Kohsuke Kawaguchi, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.security;

import org.acegisecurity.AccessDeniedException;

/**
 * Object that has an {@link ACL}
 *
 * @since 1.220
 * @see http://wiki.hudson-ci.org/display/HUDSON/Making+your+plugin+behave+in+secured+Hudson
 */
public interface AccessControlled {
    /**
     * Obtains the ACL associated with this object.
     *
     * @return never null.
     */
    ACL getACL();

    /**
     * Convenient short-cut for {@code getACL().checkPermission(permission)}
     */
    void checkPermission(Permission permission) throws AccessDeniedException;

    /**
     * Convenient short-cut for {@code getACL().hasPermission(permission)}
     */
    boolean hasPermission(Permission permission);

}
