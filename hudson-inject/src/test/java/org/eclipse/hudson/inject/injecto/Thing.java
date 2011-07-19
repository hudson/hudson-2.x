/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
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

package org.eclipse.hudson.inject.injecto;

import static com.google.common.base.Preconditions.checkState;

/**
 * Support for tests.
 */
public class Thing
{
    public SimpleComponent component;

    public void setComponent(SimpleComponent component) {
        checkState(this.component == null);
        System.out.println("Installed component: " + component);
        this.component = component;
    }
}
