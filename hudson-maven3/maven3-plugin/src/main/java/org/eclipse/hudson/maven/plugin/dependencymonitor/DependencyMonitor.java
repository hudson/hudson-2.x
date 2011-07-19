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

package org.eclipse.hudson.maven.plugin.dependencymonitor;

import org.eclipse.hudson.maven.plugin.dependencymonitor.internal.DependencyMonitorImpl;

import com.google.inject.ImplementedBy;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.DependencyGraph;
import hudson.model.TaskListener;

/**
 * Provides access to project dependency monitoring.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@ImplementedBy(DependencyMonitorImpl.class)
public interface DependencyMonitor
{
    // TODO: allow external repo/URL monitoring to be hooked up (so we can merge in the snapshot monitor plugin features)

    /**
     * Subscribe a project to receive notifications when one of its dependencies has changed.
     */
    void subscribe(AbstractProject project);

    /**
     * Unsubscribe a project from receiving notifications about dependency changes.
     */
    void unsubscribe(AbstractProject project);

    /**
     * Purge subscription details as well as any cached artifact details for the given project.
     */
    void purge(AbstractProject project);

    /**
     * Update the artifact details for the given builds project.
     */
    void update(AbstractBuild build, TaskListener listener);

    /**
     * Build the dependency graph for projects that <em>depend</em> on the given project.
     */
    void buildGraph(AbstractProject project, DependencyGraph graph);
}
