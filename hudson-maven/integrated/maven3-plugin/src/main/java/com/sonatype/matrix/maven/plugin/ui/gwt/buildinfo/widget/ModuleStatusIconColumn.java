/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.widget;

/**
 * Column to display the status of a {@link MavenProjectDTO} build.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
import com.google.gwt.resources.client.ImageResource;
import com.sonatype.matrix.gwt.common.ImageResourceColumn;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.BuildInfoResources;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.ModuleFormatter;

public class ModuleStatusIconColumn
    extends ImageResourceColumn<MavenProjectDTO>
{
    @Override
    public ImageResource getValue( MavenProjectDTO module )
    {
        return new ModuleFormatter( module ).statusIcon( BuildInfoResources.INSTANCE );
    }
}