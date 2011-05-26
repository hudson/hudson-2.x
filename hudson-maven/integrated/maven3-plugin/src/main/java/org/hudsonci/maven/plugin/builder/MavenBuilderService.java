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

package org.hudsonci.maven.plugin.builder;

import com.google.inject.ImplementedBy;
import org.hudsonci.maven.model.config.BuildConfigurationDTO;
import org.hudsonci.maven.model.state.BuildStateDTO;

import hudson.model.AbstractBuild;

import java.io.IOException;
import java.util.List;

import org.hudsonci.maven.plugin.builder.internal.MavenBuilderServiceImpl;

/**
 * Provides access to various {@link MavenBuilder} details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@ImplementedBy(MavenBuilderServiceImpl.class)
public interface MavenBuilderService
{
    BuildConfigurationDTO getBuilderDefaultConfiguration();

    void setBuilderDefaultConfiguration(BuildConfigurationDTO defaults);

    void resetBuilderDefaultConfiguration();

    /**
     * @return If no configurations returns empty list.
     */
    List<BuildConfigurationDTO> getBuilderConfigurations(String projectName);

    /**
     * @throws BuilderConfigurationNotFoundException
     */
    BuildConfigurationDTO getBuilderConfiguration(String projectName, int index);

    /**
     * @throws BuilderConfigurationNotFoundException
     */
    void setBuilderConfiguration(String projectName, int index, BuildConfigurationDTO config) throws IOException;

    /**
     * @return If no states returns empty list.
     */
    List<BuildStateDTO> getBuildStates(String projectName, final int buildNumber);

    /**
     * @return If no states returns empty list.
     */
    List<BuildStateDTO> getBuildStates(AbstractBuild build);

    /**
     * @throws BuildStateNotFoundException
     */
    BuildStateDTO getBuildState(String projectName, int buildNumber, int index);

    // TODO: Expose mavenInstallation as separate resource...  need model elements for that, also it might have sub-types?
}
