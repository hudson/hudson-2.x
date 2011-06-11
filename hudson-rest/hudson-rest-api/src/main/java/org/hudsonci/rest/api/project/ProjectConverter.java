/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.rest.api.project;

import javax.inject.Inject;

import org.hudsonci.rest.api.build.BuildConverter;
import org.hudsonci.rest.api.internal.ConverterSupport;

import org.hudsonci.rest.model.project.ProjectDTO;
import org.hudsonci.rest.model.project.ProjectReferenceDTO;
import org.hudsonci.service.SystemService;
import org.hudsonci.utils.tasks.JobUuid;

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
