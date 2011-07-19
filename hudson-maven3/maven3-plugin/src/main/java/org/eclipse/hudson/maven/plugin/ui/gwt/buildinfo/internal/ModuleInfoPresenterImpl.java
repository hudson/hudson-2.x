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

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.BuildInformationManager;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.ModuleFormatter;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.ModuleInfoPresenter;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

import javax.inject.Inject;
import javax.inject.Singleton;


import java.util.Collections;

/**
 * Default implementation of {@link ModuleInfoPresenter}.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Singleton
public class ModuleInfoPresenterImpl
    implements ModuleInfoPresenter
{
    private final ModuleInfoView view;
    private final BuildInformationManager infoManager;

    @Inject
    public ModuleInfoPresenterImpl( final ModuleInfoView view, final BuildInformationManager infoManager )
    {
        this.view = view;
        this.infoManager = infoManager;
    }

    public void setModule( final MavenProjectDTO module )
    {
        // Summary Tab
        ModuleFormatter moduleFormatter = new ModuleFormatter( module );
        view.setBuildStatus( module.getBuildSummary().getResult() );
        view.setBuildSummary( formatSummary( module, moduleFormatter ) );
        view.setCoordinates( "Coordinates: " + module.getCoordinates() );
        view.setProfileSummary( "Active profiles: " + moduleFormatter.profiles( true ) );
        view.setProducedArtifacts( infoManager.getProducedArtifacts( module.getId() ) );

        // Artifact Tab
        view.setArtifactInfo( infoManager.getConsumedArtifacts( module.getId() ) );

        view.showInfo();
    }

    public void clear()
    {
        view.hideInfo();

        view.setBuildSummary( null );
        view.setProfileSummary( null );
        view.setProducedArtifacts( Collections.<ArtifactDTO>emptyList() );
        
        view.setArtifactInfo( Collections.<ArtifactDTO>emptyList() );
    }

    private String formatSummary( final MavenProjectDTO module, final ModuleFormatter moduleFormatter )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( module.getName() ).append( formatDuration( moduleFormatter ) );
        return sb.toString();
    }

    private String formatDuration( final ModuleFormatter moduleFormatter )
    {
        String duration = moduleFormatter.duration();
        if ( duration.length() > 0 )
        {
            return " built in " + duration;
        }
        return duration;
    }
}
