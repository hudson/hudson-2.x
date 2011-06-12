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

package org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal;

import org.hudsonci.maven.model.state.BuildStateDTO;
import org.hudsonci.maven.model.state.ExecutionActivityDTO;

import javax.inject.Inject;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.BuildSummaryPresenter;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.ModuleFormatter;

import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link BuildSummaryPresenter}.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class BuildSummaryPresenterImpl implements BuildSummaryPresenter
{
    private final BuildSummaryView view;

    @Inject
    public BuildSummaryPresenterImpl( final BuildSummaryView view, final ModuleDataProvider mdp )
    {
        this.view = checkNotNull(view);
        view.setModuleData( mdp );
    }

    public void setBuildState( final BuildStateDTO buildState )
    {
        view.setBuildSummaryText( generateSummaryText( buildState ) );
    }

    private String generateSummaryText( final BuildStateDTO buildState )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "Execution of: " ).append( buildState.getBuildConfiguration().getGoals() );
        
        Date start = null;
        Date end = null;
        for ( ExecutionActivityDTO activity : buildState.getExecutionActivities() )
        {
            switch ( activity.getType() )
            {
                case STARTED:
                    start = activity.getTimestamp();
                    break;
                case FINISHED:
                    end = activity.getTimestamp();
                default:
                    break;
            }
        }
        
        if( start != null && end != null )
        {
            sb.append( " completed in " );
            sb.append( ModuleFormatter.formatTime( end.getTime() - start.getTime() ) );
        }
        return sb.toString();
    }
}
