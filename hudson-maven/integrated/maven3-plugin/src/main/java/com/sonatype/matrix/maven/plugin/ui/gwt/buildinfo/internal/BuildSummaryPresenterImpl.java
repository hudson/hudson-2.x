/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal;

import com.sonatype.matrix.maven.model.state.BuildStateDTO;
import com.sonatype.matrix.maven.model.state.ExecutionActivityDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.BuildSummaryPresenter;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.ModuleFormatter;

import javax.inject.Inject;

import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link BuildSummaryPresenter}.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
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

    @Override
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
