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

package org.eclipse.hudson.plugins.snapshotmonitor.internal;

import com.google.common.collect.Sets;

import org.eclipse.hudson.service.SystemService;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import org.eclipse.hudson.plugins.snapshotmonitor.DependenciesChangedCause;
import org.eclipse.hudson.plugins.snapshotmonitor.MetadataChecker;
import org.eclipse.hudson.plugins.snapshotmonitor.SnapshotMonitor;
import org.eclipse.hudson.plugins.snapshotmonitor.SnapshotMonitorPlugin;
import org.eclipse.hudson.plugins.snapshotmonitor.WatchedDependenciesProperty;
import org.eclipse.hudson.plugins.snapshotmonitor.model.WatchedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link SnapshotMonitor} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class SnapshotMonitorImpl
    implements SnapshotMonitor
{
    private static final Logger log = LoggerFactory.getLogger(SnapshotMonitorImpl.class);

    private final SystemService system;

    private final SnapshotMonitorPlugin plugin;

    private final Provider<MetadataChecker> checkerFactory;

    @Inject
    public SnapshotMonitorImpl(final SystemService system,
                               final SnapshotMonitorPlugin plugin,
                               final Provider<MetadataChecker> checkerFactory)
    {
        this.system = checkNotNull(system);
        this.plugin = checkNotNull(plugin);
        this.checkerFactory = checkNotNull(checkerFactory);
    }

    public boolean isConfigured() {
        return plugin.isConfigured();
    }

    private WatchedDependenciesProperty getWatchedDependenciesProperty(final AbstractProject project) throws IOException {
        assert project != null;
        // Stupid generics turd :-(
        AbstractProject<?,?> p = (AbstractProject<?,?>)project;
        WatchedDependenciesProperty property = p.getProperty(WatchedDependenciesProperty.class);
        if (property == null) {
            property = new WatchedDependenciesProperty();
            p.addProperty(property);
            log.debug("Attached watched dependencies property to project: {}", project);
        }
        return property;
    }

    private AbstractProject getProject(final AbstractBuild build) {
        assert build != null;

        if (build instanceof MatrixRun) {
            // For matrix jobs, we execute on the MatrixRun, but the trigger is configured on the MatrixProject
            return MatrixRun.class.cast(build).getParentBuild().getProject();
        }
        else {
            return build.getProject();
        }
    }

    public void update(final AbstractBuild build, final Collection<MavenCoordinatesDTO> dependencies) throws IOException {
        checkNotNull(build);
        checkNotNull(dependencies);

        if (!isConfigured()) {
            log.warn("SNAPSHOT monitor has not been configured");
            return;
        }

        log.debug("Updating dependencies: {}", dependencies);

        Set<WatchedDependency> watched = Sets.newHashSetWithExpectedSize(dependencies.size());
        for (MavenCoordinatesDTO dep : dependencies) {
            watched.add(new WatchedDependency(dep));
        }

        try {
            update(watched);
        }
        catch (IOException e) {
            log.error("Failed to check last-modified for watched dependencies", e);
        }

        AbstractProject project = getProject(build);
        WatchedDependenciesProperty property = getWatchedDependenciesProperty(project);
        property.set(watched);
        project.save();
    }

    /**
     * For each watched dependency, update its last-modified time.
     */
    private void update(final Collection<WatchedDependency> dependencies) throws IOException {
        assert dependencies != null;

        log.debug("Updating base-line for watched dependencies");

        MetadataChecker checker = checkerFactory.get();
        for (WatchedDependency watched : dependencies) {
            long result = checker.check(watched);
            watched.setLastModified(result);
        }
    }

    public void check(final AbstractProject project) throws IOException {
        checkNotNull(project);

        if (!isConfigured()) {
            log.warn("SNAPSHOT monitor has not been configured");
            return;
        }

        // Don't bother if we are shutting down
        if (system.isQuietingDown()) {
            log.debug("System is quieting down; skipping");
            return;
        }

        // Don't bother if we are blocked (queued or building)
        if (project.isBuildBlocked()) {
            log.debug("Build is blocked; skipping");
            return;
        }

        Collection<WatchedDependency> watched = getWatchedDependenciesProperty(project).get();
        if (watched.isEmpty()) {
            log.debug("No SNAPSHOT dependencies have been detected; skipping");
            return;
        }

        // Scan for any changed bits
        try {
            Collection<MavenCoordinatesDTO> changed = scan(watched);

            if (!changed.isEmpty()) {
                log.debug("Detected changed dependencies: {}", changed);

                // Double check that we are not building now, in case ^^^ took some time
                if (project.isBuildBlocked()) {
                    log.debug("Build is blocked; skipping");
                    return;
                }

                // Trigger a new build
                log.info("Triggering job because of changed dependencies: {}", project.getFullDisplayName());
                project.scheduleBuild(new DependenciesChangedCause(changed));
            }
        }
        catch (IOException e) {
            log.error("Failed to scan watched dependencies for changes: " + e, e);
        }
    }

    /**
     * For each watched dependency, check if its last-modified time is different than what we previously recorded.
     *
     * @return A set of changed dependencies; or empty if nothing was changed; never null
     */
    private Collection<MavenCoordinatesDTO> scan(final Collection<WatchedDependency> dependencies) throws IOException {
        assert dependencies != null;

        log.debug("Scanning for changed dependencies");

        Set<MavenCoordinatesDTO> changed = Sets.newHashSet();

        MetadataChecker checker = checkerFactory.get();
        for (WatchedDependency watched : dependencies) {
            long result = checker.check(watched);

            // If we changed, track the new time, and add to the changed set
            if (result != watched.getLastModified()) {
                watched.setLastModified(result);
                changed.add(watched);
            }
        }

        return changed;
    }
}
