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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.widget;

import com.google.gwt.user.cellview.client.TextColumn;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;

import static org.eclipse.hudson.maven.model.MavenCoordinatesDTOHelper.RenderStyle;

/**
 * Column to display {@link ArtifactDTO} coordinates.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
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
