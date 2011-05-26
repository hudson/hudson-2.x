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

package org.hudsonci.maven.plugin.ui.gwt.buildinfo.widget;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.ModuleFormatter;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.hudsonci.maven.model.state.BuildResultDTO;
import org.hudsonci.maven.model.state.MavenProjectDTO;

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
