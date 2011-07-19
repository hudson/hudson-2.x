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

package org.eclipse.hudson.inject;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.eclipse.hudson.inject.Smoothie;
import org.eclipse.hudson.inject.internal.SmoothieContainerBootstrap;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for inject tests.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public abstract class SmoothieTestSupport
    implements Module
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Before
    public void setUp() throws Exception {
        SmoothieUtil.reset();
        new SmoothieContainerBootstrap().bootstrap(getClass().getClassLoader(), Smoothie.class, getClass());
    }

    public void configure(final Binder binder) {
        // nop
    }

    @After
    public void tearDown() throws Exception {
        SmoothieUtil.reset();
    }
}
