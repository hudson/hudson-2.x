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

import org.eclipse.hudson.maven.model.state.BuildResultDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

import java.util.Arrays;
import java.util.Collection;

/**
 * Encapsulates the rich domain logic for the {@link MavenProjectDTO}.
 * 
 * May be used by GWT so ensure that only translatable classes are used.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class ModuleInspector
{
    private static final Collection<BuildResultDTO> completedStates = Arrays.asList(BuildResultDTO.SUCCESS, BuildResultDTO.FAILURE);

    /**
     * TODO: make duration a Long then it can be null to indicate there is no duration.
     */
    public static boolean hasDuration(MavenProjectDTO module) {
        return completedStates.contains(module.getBuildSummary().getResult());
    }
}
