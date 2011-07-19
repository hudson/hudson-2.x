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
 * Classes that are described by {@link Descriptor}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Describable<T extends Describable<T>> {
    /**
     * Gets the descriptor for this instance.
     *
     * <p>
     * {@link Descriptor} is a singleton for every concrete {@link Describable}
     * implementation, so if <tt>a.getClass()==b.getClass()</tt> then
     * <tt>a.getDescriptor()==b.getDescriptor()</tt> must hold.
     */
    Descriptor<T> getDescriptor();
}
