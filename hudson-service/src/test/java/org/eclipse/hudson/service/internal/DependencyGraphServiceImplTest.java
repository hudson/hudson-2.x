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
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;

import org.eclipse.hudson.service.DependencyGraphService;
import org.eclipse.hudson.service.internal.DependencyGraphServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DependencyGraphServiceImplTest {

    @Mock private TaskListener listener;
    @Mock private AbstractBuild<?,?> build;

    private DependencyGraphService getInst(){
        return new DependencyGraphServiceImpl();
    }

    @Test
    public void getInstNotNull() {
        assertThat(getInst(),notNullValue());
    }

    @Test(expected=NullPointerException.class)
    public void triggerDependentsNullArg1() {
        getInst().triggerDependents(null, listener);
    }

    @Test(expected=NullPointerException.class)
    public void triggerDependentsNullArg2() {
        getInst().triggerDependents(build, null);
    }


}
