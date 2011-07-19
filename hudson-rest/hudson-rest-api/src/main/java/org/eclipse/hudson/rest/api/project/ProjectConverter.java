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

package org.eclipse.hudson.rest.api.project;

import org.eclipse.hudson.rest.api.build.BuildConverter;
import javax.inject.Inject;

import org.eclipse.hudson.service.SystemService;
import org.eclipse.hudson.utils.tasks.JobUuid;

import org.eclipse.hudson.rest.api.internal.ConverterSupport;
import org.eclipse.hudson.rest.model.project.ProjectDTO;
import org.eclipse.hudson.rest.model.project.ProjectReferenceDTO;

import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converts a {@link hudson.model.AbstractProject} into a {@link ProjectDTO} object.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ProjectConverter
    extends ConverterSupport
{
    private final SystemService systemService;

    private final BuildConverter buildx;

    private final HealthConverter healthx;

    private final JobUuid jobUuid;

    @Inject
    ProjectConverter(final SystemService systemService, final BuildConverter buildx, final HealthConverter healthx, final JobUuid jobUuid) {
        this.systemService = checkNotNull(systemService);
        this.buildx = checkNotNull(buildx);
        this.healthx = checkNotNull(healthx);
        this.jobUuid = checkNotNull(jobUuid);
    }

    public ProjectDTO convert(final AbstractProject<?, ?> source) {
        checkNotNull(source);

        // FIXME a bunch of null checks are required in this method
        log.trace("Converting: {}", source);

        ProjectDTO target = new ProjectDTO();

        ProjectReferenceDTO selfRef = new ProjectReferenceDTO();
        selfRef.setId(this.jobUuid.get(source).toString());
        target.setRef(selfRef);

        target.setType(source.getClass().getName());
        target.setUrl(String.format("%s/%s", this.systemService.getUrl(), source.getUrl()));

        UUID id = this.jobUuid.get(source);
        if (id != null) {
            target.setId(id.toString());
        }

        target.setName(source.getFullName());
        target.setTitle(source.getFullDisplayName());
        target.setDescription(source.getDescription());
        target.setEnabled(!source.isDisabled());
        target.setConfigurable(source.isConfigurable());
        target.setConcurrent(source.isConcurrentBuild());
        target.setQueued(source.isInQueue());

        if (source.getLastBuild() != null) {
            target.setLastBuild(buildx.convert(source.getLastBuild()));
        }

        target.setBlocked(source.isBuildBlocked());
        target.setBlockedReason(source.getWhyBlocked());
        target.setHealth(healthx.convert(source.getBuildHealth()));

        if (source instanceof MatrixProject) {
            // Add descendants for matrix projects, only active configurations
            // for now, pending how to expose the inactive bits
            for (MatrixConfiguration child : ((MatrixProject) source).getActiveConfigurations()) {
                ProjectReferenceDTO ref = new ProjectReferenceDTO();
                ref.setId(this.jobUuid.get(child).toString());
                target.getDescendants().add(ref);
            }
        }
        else if (source instanceof MatrixConfiguration) {
            // Add parent for matrix configurations
            MatrixProject parent = ((MatrixConfiguration) source).getParent();
            ProjectReferenceDTO ref = new ProjectReferenceDTO();
            ref.setId(this.jobUuid.get(parent).toString());
            target.setParent(ref);
        }

        for (AbstractProject<?, ?> project : source.getUpstreamProjects()) {
            ProjectReferenceDTO ref = new ProjectReferenceDTO();
            ref.setId(this.jobUuid.get(project).toString());
            target.getUpstreams().add(ref);
        }

        for (AbstractProject<?, ?> project : source.getDownstreamProjects()) {
            ProjectReferenceDTO ref = new ProjectReferenceDTO();
            ref.setId(this.jobUuid.get(project).toString());
            target.getDownstreams().add(ref);
        }

        // TODO: Include transitive {up|down}stream bits?

        return target;
    }
}
