/*******************************************************************************
 *
 * Copyright (c) 2004-2011, Oracle Corporation
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
 * Partial default implementation of {@link Describable}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractDescribableImpl<T extends AbstractDescribableImpl<T>> implements Describable<T> {
    public Descriptor<T> getDescriptor() {
        return Hudson.getInstance().getDescriptorOrDie(getClass());
    }
}
