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

package org.eclipse.hudson.maven.plugin.builder;

import com.google.inject.ImplementedBy;

import org.eclipse.hudson.maven.plugin.builder.internal.MavenBuilderServiceImpl;
import org.eclipse.hudson.maven.model.config.BuildConfigurationDTO;
import org.eclipse.hudson.maven.model.state.BuildStateDTO;

import hudson.model.AbstractBuild;

import java.io.IOException;
import java.util.List;


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
