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

package org.eclipse.hudson.maven.plugin.dependencymonitor.internal;

import org.eclipse.hudson.maven.plugin.dependencymonitor.DependencyMonitor;
import org.eclipse.hudson.maven.plugin.dependencymonitor.DependencyNotifier;
import org.eclipse.hudson.maven.plugin.dependencymonitor.ProjectArtifactCache;
import org.eclipse.hudson.service.DependencyGraphService;
import org.eclipse.hudson.utils.tasks.MetaProject;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.DependecyDeclarer;
import hudson.model.DependencyGraph;
import hudson.model.DependencyGraph.Dependency;
import hudson.model.TaskListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link DependencyMonitor}.
 *
 * No specific handling of multi-configuration projects is done here.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Singleton
public class DependencyMonitorImpl
    implements DependencyMonitor
{
    private static final Logger log = LoggerFactory.getLogger(DependencyMonitorImpl.class);

    private final ProjectArtifactCache projectArtifactCache;

    private final DependencyGraphService dependencyGraphService;

    /**
     * Set of projects which are subscribed for notifications to artifact changes.
     */
    private final Set<AbstractProject> subscribedProjects = Collections.synchronizedSet(new HashSet<AbstractProject>());

    private volatile boolean primed;

    @Inject
    public DependencyMonitorImpl(final ProjectArtifactCache projectArtifactCache, final DependencyGraphService dependencyGraphService) {
        this.projectArtifactCache = checkNotNull(projectArtifactCache);
        this.dependencyGraphService = checkNotNull(dependencyGraphService);
    }

    // TODO: May need to hold onto the trigger for each project (or provide method to look it up) as it might be used to hold extra triggering configuration
    // TODO: ... like to allow/disallow triggering from external sources (whenever that feature is hooked up).

    public void subscribe(final AbstractProject project) {
        checkNotNull(project);
        log.debug("Subscribe: {}", project);

        subscribedProjects.add(project);
    }

    public void unsubscribe(final AbstractProject project) {
        checkNotNull(project);
        log.debug("Unsubscribe: {}", project);

        subscribedProjects.remove(project);
    }

    /**
     * Check if the given project is subscribed to receive artifact updated notifications.
     */
    private boolean isSubscribedForArtifactNotifications(final AbstractProject project) {
        return subscribedProjects.contains(project);
    }

    public void purge(final AbstractProject project) {
        checkNotNull(project);
        log.debug("Purge: {}", project);

        subscribedProjects.remove(project);
        projectArtifactCache.purgeArtifacts(project);
    }

    public void update(final AbstractBuild build, final TaskListener listener) {
        checkNotNull(build);
        checkNotNull(listener);
        log.debug("Update artifacts for build: {}", build);

        // If updating the artifacts mutated the cache, rebuild the dependency graph
        if (projectArtifactCache.updateArtifacts(build)) {
            dependencyGraphService.rebuild();
        }
    }

    /**
     * Find a dependency notifier for the given project.
     *
     * @return Null if no dependency notifier is found.
     */
    private DependencyNotifier findDependencyNotifier(final AbstractProject project) {
        return new MetaProject(project).getPublishersList().get(DependencyNotifier.class);
    }

    /**
     * Prime the cache, only attempts to build the cache once.
     */
    private void prime() {
        if (!primed) {
            projectArtifactCache.rebuild();
            primed = true;
            log.debug("Primed");
        }
    }

    /**
     * Build the dependency graph for projects that <em>depend</em> on the given project.
     *
     * This is invoked by the {@link DependencyNotifier} delegating its {@link DependecyDeclarer} behavior.
     */
    public void buildGraph(final AbstractProject project, final DependencyGraph graph) {
        checkNotNull(project);
        checkNotNull(graph);

        // Make sure the cache has been primed before we attempt to build anything
        prime();

        log.debug("Build dependency graph for: {}", project);

        final DependencyNotifier notifier = findDependencyNotifier(project);

        // If project is not configured to notify, then ignore it (this should not happen normally since we call buildGraph from the notifier)
        if (notifier == null) {
            log.trace("Project is not producing artifacts; skipping");
            return;
        }

        // For clarity reassign project to better name so we don't get too confused
        final AbstractProject producerProject = project;

        Collection<MavenCoordinatesDTO> producedArtifacts = projectArtifactCache.getProducedArtifacts(producerProject);
        for (AbstractProject consumerProject : projectArtifactCache.getArtifactConsumers()) {
            // Skip projects which are not subscribed to notifications
            if (!isSubscribedForArtifactNotifications(consumerProject)) {
                log.trace("Consumer project is not subscribed for notifications; skipping: {}", consumerProject);
                continue;
            }

            // If consuming any produced artifacts then it is a dependency
            Collection<MavenCoordinatesDTO> consumedArtifacts = projectArtifactCache.getConsumedArtifacts(consumerProject);
            if (CollectionsHelper.containsAny(consumedArtifacts, producedArtifacts)) {
                log.debug("{} depends on {}", consumerProject, producerProject);

                Dependency dependency = new Dependency(producerProject, consumerProject)
                {
                    @Override
                    public boolean shouldTriggerBuild(final AbstractBuild build, final TaskListener listener, final List<Action> actions) {
                        if (log.isDebugEnabled()) {
                            log.debug("Checking if build should trigger: {}; w/threshold: {}", build, notifier.getResultThreshold());
                        }
                        return build.getResult().isBetterOrEqualTo(notifier.getResultThreshold());
                    }
                };

                graph.addDependency(dependency);
            }
        }
    }
}
