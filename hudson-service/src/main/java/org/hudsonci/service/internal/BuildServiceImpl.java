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

package org.hudsonci.service.internal;

import static org.hudsonci.utils.common.Varargs.$;
import static org.hudsonci.service.internal.ServicePreconditions.*;
import hudson.model.Item;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;

import org.hudsonci.service.BuildNotFoundException;
import org.hudsonci.service.BuildService;
import org.hudsonci.service.ProjectService;
import org.hudsonci.service.SecurityService;
import org.hudsonci.service.ServiceRuntimeException;

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
