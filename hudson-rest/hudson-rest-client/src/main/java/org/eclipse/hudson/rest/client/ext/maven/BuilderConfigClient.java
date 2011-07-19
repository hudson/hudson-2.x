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

package org.eclipse.hudson.rest.client.ext.maven;

import org.eclipse.hudson.rest.client.HudsonClient;

import org.eclipse.hudson.maven.model.config.BuildConfigurationDTO;

/**
 * Client for
 * {@link org.hudsonci.maven.plugin.builder.rest.BuilderConfigResource}
 * 
 * @since 2.1.0
 */
public interface BuilderConfigClient extends HudsonClient.Extension {
    BuildConfigurationDTO getBuilderConfiguration(String projectName, int builderIndex);
    void setBuilderConfiguration(final String projectName, final int index, final BuildConfigurationDTO config);
}
