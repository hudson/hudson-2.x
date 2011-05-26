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

package org.hudsonci.service;

import org.hudsonci.service.internal.BuildServiceImpl;

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
