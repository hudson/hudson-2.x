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

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.ModuleFormatter;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.eclipse.hudson.maven.model.state.BuildResultDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

/**
 * Display of MavenProjectDTO {@link Cell}s.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
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
            sb.appendHtmlConstant( "<div class=\"" + "maven3-moduleResult" + result + "\">" );
            sb.appendEscaped( module.getName() );
            sb.appendEscaped( " " + new ModuleFormatter( module ).duration() );
            sb.appendHtmlConstant( "</div>" );
        }
    }
}
