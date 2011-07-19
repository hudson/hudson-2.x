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

import org.eclipse.hudson.service.internal.BuildServiceImpl;

import com.google.inject.ImplementedBy;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

/**
 * Operations on {@link AbstractBuild} instances.
 * <p>
 * The intent of these operations is that the build is or already has executed
 * and is available in the system.
 * <p>
 * Note: To schedule builds, see {@link ProjectService#scheduleBuild}. Since
 * scheduled builds have not necessarily been executed, that is intentionally
 * not an operation supported by this interface.
 *
 * @since 2.1.0
 */
@ImplementedBy(BuildServiceImpl.class)
public interface BuildService {

    // TODO void getConsole(AbstractProject<?, ?> project, int buildNumber);
    // TODO void getConsoleContent(AbstractProject<?, ?> project, int
    // buildNumber);
    // TODO void getTestResults(AbstractProject<?, ?> project, int buildNumber);

    /**
     * Delete a build from a project by buildNumber
     *
     * <p>
     * The current thread context must have {@link hudson.model.Run#DELETE}
     * permission to perform this operation.
     *
     * @param project the project to the build belongs to
     * @param buildNumber the buildNumber to operate on which must be
     * {@literal >} zero
     * @throws BuildNotFoundException if a build with the buildNumber could not
     * be found
     * @throws ServiceRuntimeException if an unexpected condition prevents build
     * deletion
     */
    void deleteBuild(AbstractProject<?, ?> project, int buildNumber);

    /**
     * Keep or release a build.
     *
     * <p>
     * The current thread context must have {@link hudson.model.Run#UPDATE}
     * permission to perform this operation.
     *
     * @param project the project to the build belongs to
     * @param buildNumber the buildNumber to operate on which must be
     * {@literal >} zero
     * @param release false to keep the build, true to release
     * @throws BuildNotFoundException if a build with the buildNumber could not
     * be found
     * @throws ServiceRuntimeException if an unexpected condition prevents build
     * keep or release
     */
    void keepBuild(AbstractProject<?, ?> project, int buildNumber, boolean release);

    /**
     * Stop or abort a build
     *
     * <p>
     * The current thread context must have permission to abort the build.
     *
     * @param project the project to the build belongs to
     * @param buildNumber the buildNumber to operate on which must be
     * {@literal >} zero
     * @throws BuildNotFoundException if a build with the buildNumber could not
     * be found
     * @throws ServiceRuntimeException if an unexpected condition prevents build
     * stoppage
     */
    void stopBuild(AbstractProject<?, ?> project, int buildNumber);

    /**
     * Find a build in the project by buildNumber.
     *
     * @param project
     * @param buildNumber
     * @return the build instance or null if there is no such build
     */
    AbstractBuild findBuild(AbstractProject<?, ?> project, int buildNumber);

    /**
     * Find a build in the project by buildNumber.
     *
     * @param projectName the project name used to look up the build
     * @param buildNumber the build number of the project build, which should be
     * greater than 0
     * @return the build instance or null if there is no such build
     */
    AbstractBuild findBuild(String projectName, int buildNumber);

    /**
     * Find a build in the project by buildNumber.
     *
     * @param project
     * @param buildNumber
     * @return the build instance, never null
     * @throws BuildNotFoundException if the build could not be found in the
     * project
     */
    AbstractBuild getBuild(AbstractProject<?, ?> project, int buildNumber);

    /**
     * Get a build in the project by buildNumber.
     *
     * @param projectName the project name used to look up the build
     * @param buildNumber the build number of the project build, which should be
     * greater than 0
     * @return the build instance, never null
     * @throws BuildNotFoundException if the build could not be found in the
     * project
     */
    AbstractBuild getBuild(String projectName, int buildNumber);

}
