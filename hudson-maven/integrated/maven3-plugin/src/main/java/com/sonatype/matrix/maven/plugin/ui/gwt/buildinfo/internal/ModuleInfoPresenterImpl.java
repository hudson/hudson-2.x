/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal;

import com.sonatype.matrix.maven.model.state.ArtifactDTO;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.BuildInformationManager;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.ModuleFormatter;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.ModuleInfoPresenter;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Collections;

/**
 * Default implementation of {@link ModuleInfoPresenter}.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
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

    @Override
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
    
    @Override
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
        if ( !duration.isEmpty() )
        {
            return " built in " + duration;
        }
        return duration;
    }
}
