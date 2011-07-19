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
import org.eclipse.hudson.maven.model.state.BuildStateDTO;
import java.util.List;


/**
 * Client for {@link org.hudsonci.maven.plugin.builder.rest.BuildStateResource}
 * @author plynch
 */
public interface BuildStateClient extends HudsonClient.Extension{

    BuildStateDTO getBuildState(String projectName, int buildNumber, int index);
    List<BuildStateDTO> getBuildStates(String projectName, int buildNumber);

}
