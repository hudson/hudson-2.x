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

package hudson.model;

import hudson.EnvVars;
import hudson.model.Queue.Task;
import hudson.tasks.Builder;
import hudson.tasks.BuildWrapper;

/**
 * {@link Action} that contributes environment variables during a build.
 *
 * <p>
 * For example, your {@link Builder} can add an {@link EnvironmentContributingAction} so that
 * the rest of the builders or publishers see some behavior changes.
 *
 * Another use case is for you to {@linkplain Queue#schedule(Task, int, Action...) submit a job} with
 * {@link EnvironmentContributingAction}s.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.318
 * @see AbstractBuild#getEnvironment(TaskListener)
 * @see BuildWrapper
 */
public interface EnvironmentContributingAction extends Action {
    /**
     * Called by {@link AbstractBuild} to allow plugins to contribute environment variables.
     *
     * @param build
     *      The calling build. Never null.
     * @param env
     *      Environment variables should be added to this map.
     */
    public void buildEnvVars(AbstractBuild<?,?> build, EnvVars env);
}
