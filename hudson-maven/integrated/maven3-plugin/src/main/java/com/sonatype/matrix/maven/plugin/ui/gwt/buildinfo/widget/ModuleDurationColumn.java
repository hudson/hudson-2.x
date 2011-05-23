/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.widget;

import com.google.gwt.user.cellview.client.TextColumn;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.ModuleFormatter;

/**
 * Column to display the duration of a {@link MavenProjectDTO} build.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
public class ModuleDurationColumn
    extends TextColumn<MavenProjectDTO>
{
    public ModuleDurationColumn()
    {
        super();
        setHorizontalAlignment( ALIGN_RIGHT );
    }
    
    @Override
    public String getValue( MavenProjectDTO module )
    {
        return new ModuleFormatter( module ).duration();
    }
}