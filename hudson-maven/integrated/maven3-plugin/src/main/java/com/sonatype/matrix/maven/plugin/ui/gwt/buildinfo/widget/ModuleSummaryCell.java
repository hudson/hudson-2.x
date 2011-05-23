/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.widget;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sonatype.matrix.maven.model.state.BuildResultDTO;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.ModuleFormatter;

/**
 * Display of MavenProjectDTO {@link Cell}s.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
public class ModuleSummaryCell
    extends AbstractCell<MavenProjectDTO>
{
    @Override
    public void render( Context context, MavenProjectDTO module, SafeHtmlBuilder sb )
    {
        if ( null != module )
        {
            BuildResultDTO result = module.getBuildSummary().getResult();
            sb.appendHtmlConstant( "<div class=\"" + "sonatype-moduleResult" + result + "\">" );
            sb.appendEscaped( module.getName() );
            sb.appendEscaped( " " + new ModuleFormatter( module ).duration() );
            sb.appendHtmlConstant( "</div>" );
        }
    }
}