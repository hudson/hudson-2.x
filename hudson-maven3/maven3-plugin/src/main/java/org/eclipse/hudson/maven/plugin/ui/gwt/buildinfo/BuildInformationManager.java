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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo;

import com.google.inject.ImplementedBy;

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.BuildInformationManagerImpl;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;

import java.util.Collection;


/**
 * Manages access to the build information.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@ImplementedBy(BuildInformationManagerImpl.class)
public interface BuildInformationManager
{
    void refresh();

    Collection<ArtifactDTO> getConsumedArtifacts(String moduleId);

    Collection<ArtifactDTO> getProducedArtifacts(String moduleId);
}
