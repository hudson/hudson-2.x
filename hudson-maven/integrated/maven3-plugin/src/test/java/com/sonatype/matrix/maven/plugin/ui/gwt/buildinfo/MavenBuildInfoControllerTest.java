/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo;

import com.google.gwt.view.client.HasData;
import com.sonatype.matrix.maven.model.state.ArtifactDTO;
import com.sonatype.matrix.maven.model.state.BuildStateDTO;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.event.BuildStateSelectedEvent;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.ArtifactDataProvider;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.ModuleDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Interaction test of {@link MavenBuildInfoController}.
 * 
 * @author Jamie Whitehouse
 */
@RunWith( MockitoJUnitRunner.class )
public class MavenBuildInfoControllerTest
{
    @Mock(answer=Answers.RETURNS_DEEP_STUBS)
    private HasData<MavenProjectDTO> mdpDisplay;

    @Mock(answer=Answers.RETURNS_DEEP_STUBS)
    private HasData<ArtifactDTO> adpDisplay;

    @SuppressWarnings( "unchecked" )
    @Test
    public void selectingBuildStateUpdatesDataProviders()
    {
        ModuleDataProvider mdp = new ModuleDataProvider();
        mdp.addDataDisplay( mdpDisplay );

        ArtifactDataProvider adp = new ArtifactDataProvider();
        adp.addDataDisplay( adpDisplay );
        
        MavenBuildInfoController controller = new MavenBuildInfoController( null, null, null, mdp, adp, mock( BuildSummaryPresenter.class ), mock( ModuleInfoPresenter.class ), mock( ModuleInfoPickerPresenter.class ) );
        
        // Select build state.
        MavenProjectDTO expectedModule = new MavenProjectDTO().withName( "a test maven module" );
        BuildStateDTO buildState = new BuildStateDTO().withParticipatingProjects( expectedModule );
        controller.buildStateSelected( new BuildStateSelectedEvent( buildState ) );
        
        // Backing list was updated.
        assertThat( mdp.getList(), contains( expectedModule ) );
        
        // Attached views were updated.
        verify( mdpDisplay ).setRowData( anyInt(), anyList() );
        verify( adpDisplay ).setRowData( anyInt(), anyList() );
    }
}
