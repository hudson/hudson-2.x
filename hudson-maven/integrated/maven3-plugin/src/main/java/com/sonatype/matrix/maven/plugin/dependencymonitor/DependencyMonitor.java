/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.dependencymonitor;

import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.plugin.dependencymonitor.internal.DependencyMonitorImpl;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.DependencyGraph;
import hudson.model.TaskListener;

/**
 * Provides access to project dependency monitoring.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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
