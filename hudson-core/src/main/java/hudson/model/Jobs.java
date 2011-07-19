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

package hudson.model;

import hudson.util.DescriptorList;
import hudson.Extension;

import java.util.List;

/**
 * List of all installed {@link Job} types.
 * 
 * @author Kohsuke Kawaguchi
 * @deprecated since 1.281
 */
public class Jobs {
    /**
     * List of all installed {@link JobPropertyDescriptor} types.
     *
     * <p>
     * Plugins can add their {@link JobPropertyDescriptor}s to this list.
     *
     * @see JobPropertyDescriptor#getPropertyDescriptors(Class)
     *
     * @deprecated as of 1.281
     *      Use {@link JobPropertyDescriptor#all()} for read access,
     *      and {@link Extension} for registration.
     */
    public static final List<JobPropertyDescriptor> PROPERTIES = (List)
            new DescriptorList<JobProperty<?>>((Class)JobProperty.class);
}
