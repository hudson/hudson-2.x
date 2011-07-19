/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.matrix;

import hudson.Extension;

/**
 * {@link AxisDescriptor} for manually entered default axis.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class DefaultAxisDescriptor extends AxisDescriptor {
    public DefaultAxisDescriptor() {
        super(Axis.class);
    }

    @Override
    public String getDisplayName() {
        return "Axis";
    }

    @Override
    public boolean isInstantiable() {
        return false;
    }    
}
