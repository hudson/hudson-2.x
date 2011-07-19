/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *        
 *
 *******************************************************************************/ 

package hudson.tools;

import hudson.Extension;

/**
 * Descriptor for {@link ToolProperty}.
 *
 * <p>
 * Put {@link Extension} on your descriptor implementation to have it auto-registered.
 *
 * @since 1.286
 * @see ToolProperty
 * @author Kohsuke Kawaguchi
 */
public abstract class ToolPropertyDescriptor extends PropertyDescriptor<ToolProperty<?>,ToolInstallation> {
    protected ToolPropertyDescriptor(Class<? extends ToolProperty<?>> clazz) {
        super(clazz);
    }

    protected ToolPropertyDescriptor() {
    }
}

