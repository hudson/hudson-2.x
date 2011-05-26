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

package org.hudsonci.maven.model.state;

import org.hudsonci.maven.model.MavenCoordinatesDTO;
import org.hudsonci.maven.model.MavenCoordinatesDTOHelper;
import org.hudsonci.maven.model.MavenCoordinatesDTOHelper.RenderStyle;

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
