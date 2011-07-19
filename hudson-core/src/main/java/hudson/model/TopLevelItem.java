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
import hudson.ExtensionPoint;
import hudson.matrix.MatrixConfiguration;

/**
 * {@link Item} that can be directly displayed under {@link Hudson} or other containers.
 * Ones that don't need to be under specific parent (say, unlike {@link MatrixConfiguration}),
 * and thus can be freely moved, copied, etc.
 *
 * <p>
 * To register a custom {@link TopLevelItem} class from a plugin, put {@link Extension} on your
 * {@link TopLevelItemDescriptor}. Also see {@link Items#XSTREAM}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface TopLevelItem extends Item, ExtensionPoint, Describable<TopLevelItem> {
    /**
     *
     * @see Describable#getDescriptor()
     */
    TopLevelItemDescriptor getDescriptor();
}
