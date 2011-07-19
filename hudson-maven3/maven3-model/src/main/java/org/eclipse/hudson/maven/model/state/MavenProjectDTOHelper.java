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

package org.eclipse.hudson.maven.model.state;

import org.eclipse.hudson.maven.model.MavenCoordinatesDTOHelper;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTOHelper.RenderStyle;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

/**
 * Helper for {@link MavenProjectDTO}.
 *
 * The id used within the system should always be generated from this helper because there are inconsistencies
 * with MavenSession.getId and MavenProject.getId .
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class MavenProjectDTOHelper
{
    /**
     * Rendering style for generating the internally used ID.
     */
    private static final RenderStyle ID_STYLE = RenderStyle.GATV;

    // FIXME: Start using MavenCoordinates instead of string based id.
    // Using coordinates can inflate the size of the xml, not as much as an issue now that attributes are used in the xml.

    /**
     * @return the project id as groupId:artifactId:packaging:version
     */
    public static String asId( final MavenProjectDTO source ) {
        return MavenCoordinatesDTOHelper.asString(source.getCoordinates(), ID_STYLE);
    }
    
    /**
     * @return the project id as groupId:artifactId:packaging:version
     */
    public static String asId( final MavenCoordinatesDTO source ) {
        return MavenCoordinatesDTOHelper.asString(source, ID_STYLE);
    }
}
