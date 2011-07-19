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

package org.eclipse.hudson.legacy.maven.plugin;

import hudson.model.Action;

import java.util.List;
import java.util.Map;

/**
 * Indicates that this {@link Action} for {@link MavenBuild} contributes
 * an "aggregated" action to {@link MavenBuild#getModuleSetBuild()
 * its governing MavenModuleSetBuild}. 
 *
 * @author Kohsuke Kawaguchi
 * @since 1.99
 * @see MavenReporter
 */
public interface AggregatableAction extends Action {
    /**
     * Creates {@link Action} to be contributed to {@link MavenModuleSetBuild}.
     *
     * @param build
     *      {@link MavenModuleSetBuild} for which the aggregated report is
     *      created.
     * @param moduleBuilds
     *      The result of {@link MavenModuleSetBuild#getModuleBuilds()} provided
     *      for convenience and efficiency.
     * @return
     *      null if the reporter provides no such action.
     */
    MavenAggregatedReport createAggregatedAction(
        MavenModuleSetBuild build, Map<MavenModule,List<MavenBuild>> moduleBuilds);
}
