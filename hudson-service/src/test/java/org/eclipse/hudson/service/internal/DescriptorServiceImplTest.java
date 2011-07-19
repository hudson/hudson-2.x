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

package org.eclipse.hudson.service.internal;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.eclipse.hudson.service.internal.DescriptorServiceImpl;
import org.junit.Test;

public class DescriptorServiceImplTest {

    private DescriptorServiceImpl getInst() {
        return new DescriptorServiceImpl();
    }

    @Test
    public void getInstNotNull() {
        assertThat(getInst(),notNullValue());
    }

    @Test(expected = NullPointerException.class)
    public void getDescriptorNullArg() {
        getInst().getDescriptor((String) null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test(expected = NullPointerException.class)
    public void getDescriptorByTypeNullArg() {
        getInst().getDescriptor((Class) null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test(expected = NullPointerException.class)
    public void getDescriptorOrDieByTypeNullArg() {
        getInst().getDescriptorOrDie((Class) null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test(expected = NullPointerException.class)
    public void getDescriptorByType() {
        getInst().getDescriptorByType((Class) null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test(expected = NullPointerException.class)
    public void getDescriptorList() {
        getInst().getDescriptorList((Class) null);
    }

}
