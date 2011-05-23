/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.dependencymonitor.internal;

import com.sonatype.matrix.maven.plugin.dependencymonitor.DependencyMonitor;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Hook to purge records when a project is deleted.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Named
@Singleton
public class ProjectDeletedHandler
    extends ItemListener
{
    private static final Logger log = LoggerFactory.getLogger(ProjectDeletedHandler.class);

    private final DependencyMonitor dependencyMonitor;

    @Inject
    public ProjectDeletedHandler(final DependencyMonitor dependencyMonitor) {
        this.dependencyMonitor = checkNotNull(dependencyMonitor);
    }

    @Override
    public void onDeleted(final Item item) {
        if (item instanceof AbstractProject) {
            AbstractProject project = (AbstractProject)item;
            log.debug("Project has been deleted; puring: {}", project);

            dependencyMonitor.purge(project);
        }
    }
}

