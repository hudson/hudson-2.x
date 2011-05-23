/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder;

import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.model.config.BuildConfigurationDTO;
import com.sonatype.matrix.maven.model.state.BuildStateDTO;
import com.sonatype.matrix.maven.plugin.builder.internal.MavenBuilderServiceImpl;

import hudson.model.AbstractBuild;

import java.io.IOException;
import java.util.List;

/**
 * Provides access to various {@link MavenBuilder} details.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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