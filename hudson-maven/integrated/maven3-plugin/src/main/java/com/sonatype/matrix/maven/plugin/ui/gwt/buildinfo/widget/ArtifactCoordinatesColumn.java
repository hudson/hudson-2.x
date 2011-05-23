/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.widget;

import com.google.gwt.user.cellview.client.TextColumn;
import com.sonatype.matrix.maven.model.state.ArtifactDTO;

import static com.sonatype.matrix.maven.model.MavenCoordinatesDTOHelper.RenderStyle;

/**
 * Column to display {@link ArtifactDTO} coordinates.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
public class ArtifactCoordinatesColumn
    extends TextColumn<ArtifactDTO>
{
    @Override
    public String getValue( ArtifactDTO artifact )
    {
        return artifact.getCoordinates().toString(RenderStyle.GATCV_OPTIONAL);
    }
}