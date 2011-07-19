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

package org.eclipse.hudson.service;

import org.eclipse.hudson.service.internal.DependencyGraphServiceImpl;

import hudson.model.DependencyGraph;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;

import com.google.inject.ImplementedBy;

/**
 * {@link DependencyGraph} related services.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@ImplementedBy(DependencyGraphServiceImpl.class)
public interface DependencyGraphService {
    DependencyGraph getGraph();

    void rebuild();

    void triggerDependents(AbstractBuild<?, ?> build, TaskListener listener);
}
