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

package org.eclipse.hudson.service.internal;

import static org.eclipse.hudson.service.internal.ServicePreconditions.*;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.model.AbstractProject;
import hudson.model.Job;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.hudson.service.ProjectNotFoundException;
import org.eclipse.hudson.service.ProjectService;
import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.service.ServiceRuntimeException;
import org.eclipse.hudson.service.SystemIntegrityViolationException;
import org.eclipse.hudson.utils.tasks.JobUuid;

import com.google.common.base.Preconditions;


/**
 * Default {@link ProjectService} implementation.
 *
 * @since 2.1.0
 */
@Named
@Singleton
public class ProjectServiceImpl extends ServiceSupport implements ProjectService {
    private final SecurityService securityService;

    @Inject
    ProjectServiceImpl(final SecurityService securityService) {
        this.securityService = Preconditions.checkNotNull(securityService);
    }


    public AbstractProject<?, ?> getProject(final UUID id) {
        AbstractProject<?, ?> project = findProject(id);
        if (project == null) {
            throw new ProjectNotFoundException(String.format("No project with UUID '%s' found.", id));
        }
        return project;
    }

    public AbstractProject<?, ?> getProject(final String projectName) {
        AbstractProject<?, ?> project = findProject(projectName);
        if (project == null) {
            throw new ProjectNotFoundException(String.format("Project %s not found.", projectName));
        }
        return project;
    }

    public AbstractProject<?, ?> getProjectByFullName(final String projectName) {
        AbstractProject<?, ?> p = findProjectByFullName(projectName);
        if (p == null) {
            throw new ProjectNotFoundException(String.format("Project %s not found.", projectName));
        }
        return p;
    }

    public AbstractProject<?, ?> findProject(final UUID id) {
        checkNotNull(id, UUID.class);
        Job<?, ?> job = JobUuid.find(id);
        return (AbstractProject<?, ?>) job;
    }

    public AbstractProject<?, ?> findProject(final String projectName) {
        checkProjectName(projectName);

        // Handle matrix-project/configuration projects.
        // First part is matrix-project name, second part is
        // matrix-configuration name
        String[] parts = projectName.split("/", 2);
        if (parts.length == 2) {
            log.debug("Detected matrix name: {}", projectName);
            AbstractProject<?, ?> parent = findProject(parts[0]);
            if (parent instanceof MatrixProject) {
                for (MatrixConfiguration config : ((MatrixProject) parent).getItems()) {
                    if (parts[1].equals(config.getName())) {
                        log.debug("Selected matrix configuration: {}", config);
                        // config.checkPermission(Item.READ); // TODO needed?
                        return config;
                    }
                }
            }
        }
        if (!getProjectNames().contains(projectName)) {
            log.debug("Project {} not in the list of job names.", projectName);
            return null;
        }
        AbstractProject<?, ?> project = getProjectByFullName(projectName);
        log.debug("Selected project: {}", project);
        return project;
    }

    public AbstractProject<?, ?> findProjectByFullName(final String projectName) {
        checkProjectName(projectName);
        AbstractProject<?, ?> p = getHudson().getItemByFullName(projectName, AbstractProject.class);
        if (p != null) {
            this.securityService.checkPermission(p, Item.READ);
        }
        return p;
    }

    @SuppressWarnings("rawtypes")
    public List<AbstractProject> getAllProjects() {
        // Hudson checks that items are readable by the current context (has Item.READ perms)
        return getHudson().getAllItems(AbstractProject.class);
    }

    public <T extends AbstractProject<?, ?>> T copyProject(final T src, final String targetProjectName)
            throws ServiceRuntimeException {
        checkNotNull(src, AbstractProject.class);
        checkProjectName(targetProjectName);

        this.securityService.checkPermission(Item.CREATE);
        this.securityService.checkPermission(src, Item.EXTENDED_READ);
        // caller should try to verify this themselves before calling me
        // TODO should this check really be performed here?
        if (projectExists(targetProjectName)) {
            throw new SystemIntegrityViolationException(String.format("Project %s already exists.", targetProjectName));

        }

        try {
            return getHudson().copy(src, targetProjectName);
        } catch (IOException ex) {
            throw new ServiceRuntimeException(String.format("Project copy failed from %s to %s", src.getName(),
                    targetProjectName), ex);
        }
    }

    public TopLevelItem createProjectFromXML(final String projectName, final InputStream xml) {
        checkProjectName(projectName);
        checkNotNull(xml, InputStream.class);

        this.securityService.checkPermission(Item.CREATE);

        // caller should verify this themselves before calling me
        // TODO should this check really be performed here?
        if (projectExists(projectName)) {
            throw new SystemIntegrityViolationException(String.format("Project %s already exists.", projectName));
        }

        try {
            return getHudson().createProjectFromXML(projectName, xml);
        } catch (IOException ex) {
            throw new ServiceRuntimeException(String.format("Project creation failed for %s", projectName), ex);
        }
    }

    public boolean projectExists(final String projectName) {
        checkProjectName(projectName);
        // This only checks names that are readable by the current context (has Item.READ perms)
        return getProjectNames().contains(projectName);
    }

    public Collection<String> getProjectNames() {
        // Hudson only returns jobnames when the current context has Item.READ perms on it
        return getHudson().getJobNames();
    }

}
