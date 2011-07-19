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

package org.eclipse.hudson.service;

import com.google.inject.ImplementedBy;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.TopLevelItem;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.eclipse.hudson.service.internal.ProjectServiceImpl;

/**
 * Service API related to Projects and Job models, such as that by {@link AbstractProject}.
 *
 * @author plynch
 * @since 2.1.0
 */
@ImplementedBy(ProjectServiceImpl.class)
public interface ProjectService
{
    /**
     * Get the project identified by a UUID.
     *
     * @param id UUID identifier of the project to get
     * @return the project associated with the UUID
     * @throws ProjectNotFoundException if a project cannot be found for the given UUID
     */
    AbstractProject<?, ?> getProject(final UUID id);

    /**
     * Get the project identified by a UUID.
     *
     * @param id UUID identifier of the project to get
     * @return the project associated with the UUID or null if not found
     */
    AbstractProject<?, ?> findProject(final UUID id);

    /**
     * Get the project identified by a full project name.
     *
     * The current thread context must have {@link Item#READ} permission on the project in order to
     * get it.
     *
     * @param projectFullName full project name as reported by {@link AbstractProject#getFullName}
     * @return the project associated with projectFullName
     * @throws ProjectNotFoundException if a project with the specified full name does not exist
     */
    AbstractProject<?, ?> getProjectByFullName(String projectFullName);

    /**
     * Find the project identified by a full project name.
     *
     * The current thread context must have {@link Item#READ} permission on the project in order to
     * find it.
     *
     * @param projectFullName full project name as reported by {@link AbstractProject#getFullName}
     * @return the project associated with projectFullName or null if not found
     */
    AbstractProject<?, ?> findProjectByFullName(String projectFullName);

    /**
     * Check if a project with given name as reported by {@link AbstractProject#getName}
     * already exists in the system.
     *
     * In order to determine if a project exists, the current thread context must have {@link Item#READ}
     * permission on it.
     *
     * @param projectName project name to lookup
     * @return true if a project with the specified name is readable and exists in the system
     */
    boolean projectExists(String projectName);

    /**
     * Get a list of all projects of a specific {@literal type} in the system.
     *
     * <p>The current thread must have {@link Item#READ} permission
     * on each project returned and {@link Item#READ} permission on the {@link hudson.model.Node}
     * where the projects reside.
     *
     * @return a list of all projects implemented by type T
     */
    @SuppressWarnings("rawtypes")
    List<AbstractProject> getAllProjects();

    /**
     * Copy a source project to a new project which will be named the
     * value of {@literal targetProjectName}
     *
     * <p>The current thread must have {@link Item#EXTENDED_READ} permission
     * on {@literal src} and {@link Item#CREATE} permission on the {@link hudson.model.Node}
     * where the project will be copied too, in order for the operation to succeed.
     *
     * @param <T> the type of new target project
     * @param src source project to copy from
     * @param targetProjectName the name to create the new project with
     * @return the newly created target project
     * @throws SystemIntegrityViolationException if a project already exists with the targetProjectName
     * @throws ServiceRuntimeException if there was an unexpected problem creating the project
     */
    <T extends AbstractProject<?, ?>> T copyProject(T src, String targetProjectName);

    /**
     * Create a new project in the system from XML.
     *
     * <p>The current thread must have {@link hudson.model.Item#CREATE} permission on the
     * {@link hudson.model.Node} where the project will created, in order for the operation to
     * succeed.
     *
     * <p>Buffering or closing of the InputStream should be done outside of this method.
     *
     * @param projectName the name of the new project.
     * @param xml XML document describing the new project
     * @return a {@link hudson.model.TopLevelItem} representing the newly created project
     * @throws SystemIntegrityViolationException if a project already exists with projectName
     * @throws ServiceRuntimeException if there was an unexpected problem creating the project
     */
    TopLevelItem createProjectFromXML(String projectName, InputStream xml);

    /**
     * Find and return a project with the given name ( as defined by {@link AbstractProject#getName} )
     * , or return {@literal null} if one cannot be found.
     *
     * <p>The current thread context must have {@link Item#READ} permission on the project in order to find it.
     *
     * @param projectName
     * @return a project for the specified name if it is readable and exists, otherwise null
     */
    AbstractProject<?,?> findProject(String projectName);

    /**
     * Tries to find a project with the given name as defined by {@link AbstractProject#getName}.
     *
     * <p>The current thread context must have {@link Item#READ} permission on the project in order to find it.
     *
     * @param projectName the project name for the project
     * @return an AbstractProject for the specified name if one exists, never null
     * @throws ProjectNotFoundException if the project cannot be found
     */
    AbstractProject<?,?> getProject(String projectName);

    /**
     * Return a collection of all project names in the system.
     *
     * <p>{@link Item#READ} access is required to return a job name in the collection.
     *
     * @return a collection, never null, of all project names in the system
     */
    Collection<String> getProjectNames();
}
