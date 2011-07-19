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

import org.eclipse.hudson.service.DependencyGraphService;
import org.eclipse.hudson.utils.plugin.ui.JellyAccessible;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.DependecyDeclarer;
import hudson.model.DependencyGraph;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Notify the {@link DependencyMonitor} after a build has completed about produced and consumed Maven artifacts.
 * Projects which notify are artifact producers.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@XStreamAlias("maven-dependency-notifier")
public class DependencyNotifier
    extends Notifier
    implements DependecyDeclarer, MatrixAggregatable
{
    private static final Logger log = LoggerFactory.getLogger(DependencyNotifier.class);

    private final boolean notifyIfUnstable;

    @DataBoundConstructor
    public DependencyNotifier(final boolean notifyIfUnstable) {
        this.notifyIfUnstable = notifyIfUnstable;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @JellyAccessible
    public boolean isNotifyIfUnstable() {
        return notifyIfUnstable;
    }

    public Result getResultThreshold() {
        return notifyIfUnstable ? Result.UNSTABLE : Result.SUCCESS;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    /**
     * @see org.eclipse.hudson.maven.plugin.dependencymonitor.internal.BuildArtifactsUpdater#onCompleted
     */
    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        return true;
    }

    public MatrixAggregator createAggregator(final MatrixBuild build, final Launcher launcher, final BuildListener listener) {
        return new MatrixAggregator(build, launcher, listener)
        {
            // TODO: Could potentially figure out if we need to rebuild the graph, but still have to trigger here

            @Override
            public boolean endBuild() throws InterruptedException, IOException {
                // For multi-configuration projects we have to rebuild the graph and trigger dependents manually :-(
                log.debug("Forcing rebuild of graph for multi-config project: {}", build);
                getDescriptor().dependencyGraphService.rebuild();
                getDescriptor().dependencyGraphService.triggerDependents(build, listener);
                return true;
            }
        };
    }

    /**
     * Delegate to the {@link DependencyMonitor} to build the graph of projects which depend on the given project.
     */
    public void buildDependencyGraph(final AbstractProject project, final DependencyGraph graph) {
        getDescriptor().dependencyMonitor.buildGraph(project, graph);
    }

    @Named
    @Singleton
    @Typed(Descriptor.class)
    public static class DescriptorImpl
        extends BuildStepDescriptor<Publisher>
    {
        private final DependencyMonitor dependencyMonitor;

        private final DependencyGraphService dependencyGraphService;

        @Inject
        public DescriptorImpl(final DependencyMonitor dependencyMonitor, final DependencyGraphService dependencyGraphService) {
            this.dependencyMonitor = checkNotNull(dependencyMonitor);
            this.dependencyGraphService = checkNotNull(dependencyGraphService);
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> type) {
            return Project.class.isAssignableFrom(type) || MatrixProject.class.isAssignableFrom(type);
        }

        @Override
        public String getDisplayName() {
            // TODO: i18n
            return "Notify that Maven dependencies have been updated by Maven" +
                    " 3 integration";
        }
    }
}
