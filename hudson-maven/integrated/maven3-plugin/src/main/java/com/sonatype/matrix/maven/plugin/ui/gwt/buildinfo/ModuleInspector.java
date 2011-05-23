/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo;

import com.sonatype.matrix.maven.model.state.BuildResultDTO;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;

import java.util.Arrays;
import java.util.Collection;

/**
 * Encapsulates the rich domain logic for the {@link MavenProjectDTO}.
 * 
 * May be used by GWT so ensure that only translatable classes are used.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
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
