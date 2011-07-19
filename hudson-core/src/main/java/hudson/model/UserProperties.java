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

import hudson.Extension;
import hudson.util.DescriptorList;

import java.util.List;

/**
 * List of all installed {@link UserProperty} types.
 * @author Kohsuke Kawaguchi
 * @deprecated as of 1.286
 */
public class UserProperties {
    /**
     * @deprecated as of 1.286
     *      Use {@link UserProperty#all()} for read access and {@link Extension} for auto-registration.
     */
    public static final List<UserPropertyDescriptor> LIST = (List)new DescriptorList<UserProperty>(UserProperty.class);
}
