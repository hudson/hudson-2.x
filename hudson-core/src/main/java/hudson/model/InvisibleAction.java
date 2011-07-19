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

package hudson.model;

/**
 * Partial {@link Action} implementation that doesn't have any UI presence.
 *
 * <p>
 * This class can be used as a convenient base class, when you use
 * {@link Action} for just storing data associated with a build.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.188
 */
public abstract class InvisibleAction implements Action {
    public final String getIconFileName() {
        return null;
    }

    public final String getDisplayName() {
        return null;
    }

    public final String getUrlName() {
        return null;
    }
}
