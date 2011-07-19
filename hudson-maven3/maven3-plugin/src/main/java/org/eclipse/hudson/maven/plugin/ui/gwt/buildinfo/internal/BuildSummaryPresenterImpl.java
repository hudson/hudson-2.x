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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal;

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.BuildSummaryPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.ModuleFormatter;
import org.eclipse.hudson.maven.model.state.BuildStateDTO;
import org.eclipse.hudson.maven.model.state.ExecutionActivityDTO;

import javax.inject.Inject;


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
