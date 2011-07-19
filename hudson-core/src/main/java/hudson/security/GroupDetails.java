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

/**
 * Represents the details of a group.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.280
 * @see UserDetails
 */
public abstract class GroupDetails {
    /**
     * Returns the name of the group.
     *
     * @return never null.
     */
    public abstract String getName();

    /**
     * Returns the human-readable name used for rendering in HTML.
     *
     * <p>
     * This may contain arbitrary character, and it can change.
     *
     * @return never null.
     */
    public String getDisplayName() {
        return getName();
    }
}
