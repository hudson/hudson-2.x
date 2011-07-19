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
import static org.eclipse.hudson.utils.common.Varargs.$;
import hudson.model.Item;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;

import org.eclipse.hudson.service.BuildNotFoundException;
import org.eclipse.hudson.service.BuildService;
import org.eclipse.hudson.service.ProjectService;
import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.service.ServiceRuntimeException;

import com.google.common.base.Preconditions;

/**
 * Default implementation of {@link BuildService}.
 *
 * @since 2.1.0
 */
@Named
@Singleton
public class BuildServiceImpl extends ServiceSupport implements BuildService {
    private final ProjectService projects;
    private final SecurityService security;

    @Inject
    BuildServiceImpl(final ProjectService projects, SecurityService security) {
        this.projects = Preconditions.checkNotNull(projects);
        this.security = Preconditions.checkNotNull(security);
    }

    public void deleteBuild(final AbstractProject<?, ?> project, final int buildNumber) {
        AbstractBuild<?, ?> build = getBuild(project, buildNumber);
        this.security.checkPermission(build, Run.DELETE);
        log.debug("Deleting build: {} #{}", project.getName(), buildNumber);
        try {
            build.delete();
        } catch (IOException e) {
            throw new ServiceRuntimeException("Delete failed for build " + project.getName() + " #" + buildNumber, e);
        }
    }

    public void keepBuild(final AbstractProject<?, ?> project, final int buildNumber, final boolean release) {
        AbstractBuild<?, ?> build = getBuild(project, buildNumber);
        this.security.checkPermission(build, Run.UPDATE);
        log.debug("{} build: {} #{}", $(release ? "Releasing" : "Keeping", project.getName(), buildNumber));

        try {
            build.keepLog(!release);
        } catch (IOException e) {
            throw new ServiceRuntimeException((release ? "Releasing failed for build #" : "Keeping failed for build ")
                    + project.getName() + " #" + buildNumber);
        }
    }

    public AbstractBuild<?, ?> getBuild(final String projectName, final int buildNumber) {
        checkProjectName(projectName);
        checkBuildNumber(buildNumber);

        AbstractProject<?, ?> project = projects.getProject(projectName);
        return getBuild(project, buildNumber);
    }

    public AbstractBuild<?, ?> getBuild(final AbstractProject<?,?> project, final int buildNumber)
            throws BuildNotFoundException {
        AbstractBuild<?, ?> build = findBuild(project, buildNumber);
        if (build == null) {
            throw new BuildNotFoundException("Build " + project.getName() + " #" + buildNumber + " could not be found.");
        }
        return build;
    }

    public AbstractBuild<?, ?> findBuild(final String projectName, final int buildNumber) {
        checkProjectName(projectName);
        checkBuildNumber(buildNumber);

        AbstractProject<?, ?> project = projects.findProject(projectName);
        return project != null ? findBuild(project, buildNumber) : null;
    }

    public AbstractBuild<?,?> findBuild(final AbstractProject<?, ?> project, final int buildNumber) {
        checkNotNull(project, "project");
        checkBuildNumber(buildNumber);

        AbstractBuild<?,?> build = project.getBuildByNumber(buildNumber);

        if (build != null) {
            this.security.checkPermission(build, Item.READ);
        }

        return build;
    }

    public void stopBuild(final AbstractProject<?, ?> project, final int buildNumber){
        AbstractBuild<?, ?> build = getBuild(project, buildNumber);
        log.debug("Stopping build: {} #{}", project.getName(), buildNumber);
        try {
            // Security: doStop eventually checks to see if the task owner has permission to abort the build
            build.doStop(DummyStaplerRequest.INSTANCE, DummyStaplerResponse.INSTANCE);
        } catch (IOException e) {
            throw new ServiceRuntimeException("Stop failed for " + project.getName() + " #" + buildNumber, e);
        } catch (ServletException e) {
            throw new ServiceRuntimeException("Stop failed for " + project.getName() + " #" + buildNumber, e);
        }
    }

}
