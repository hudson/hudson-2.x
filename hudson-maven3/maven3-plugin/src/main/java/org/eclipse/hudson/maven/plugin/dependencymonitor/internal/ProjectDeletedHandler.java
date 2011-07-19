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

import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;

import org.eclipse.hudson.maven.plugin.dependencymonitor.DependencyMonitor;
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
 * @since 2.1.0
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

