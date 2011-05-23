/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.plugins.snapshotmonitor;

import com.google.common.collect.ImmutableSet;
import com.sonatype.matrix.plugins.snapshotmonitor.internal.WatchedDependenciesLoader;
import com.sonatype.matrix.plugins.snapshotmonitor.model.WatchedDependencies;
import com.sonatype.matrix.plugins.snapshotmonitor.model.WatchedDependency;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Property to attach {@link WatchedDependencies} to a project.
 *
 * A property is required to more easily hook up the dependencies when the project loads.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@XStreamAlias("watched-dependencies-property")
public class WatchedDependenciesProperty
    extends JobProperty<AbstractProject<?,?>>
{
    private static final Logger log = LoggerFactory.getLogger(WatchedDependenciesProperty.class);

    @XStreamOmitField
    private WatchedDependencies watchedDependencies;

    @DataBoundConstructor
    public WatchedDependenciesProperty() {
        // all data handled externally
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Override
    protected void setOwner(final AbstractProject<?, ?> owner) {
        super.setOwner(owner);
        try {
            watchedDependencies = getDescriptor().loader.load(owner);
        }
        catch (IOException e) {
            log.error("Failed to load watched dependencies for: {}", owner, e);
        }
    }

    public synchronized Collection<WatchedDependency> get() {
        Collection<WatchedDependency> deps = watchedDependencies.getDependencies();
        if (deps == null) {
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(deps);
    }

    public synchronized void set(final Collection<WatchedDependency> dependencies) {
        checkNotNull(dependencies);
        watchedDependencies.setDependencies(new HashSet<WatchedDependency>(dependencies));
        try {
            getDescriptor().loader.store(owner, watchedDependencies);
        }
        catch (IOException e) {
            log.error("Failed to update watched dependencies for: {}", owner, e);
        }
    }

    @Named
    @Singleton
    @Typed(Descriptor.class)
    public static class DescriptorImpl
        extends JobPropertyDescriptor
    {
        private final WatchedDependenciesLoader loader;

        @Inject
        public DescriptorImpl(final WatchedDependenciesLoader loader) {
            this.loader = checkNotNull(loader);
        }

        @Override
        public String getDisplayName() {
            return "Watched dependencies property"; // AFAICT not used anywhere, but required
        }

        @Override
        public boolean isApplicable(final Class<? extends Job> type) {
            return true;
        }
    }
}