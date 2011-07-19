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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo;

import com.google.gwt.view.client.HasData;

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.BuildSummaryPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.MavenBuildInfoController;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.ModuleInfoPickerPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.ModuleInfoPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.event.BuildStateSelectedEvent;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.ArtifactDataProvider;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.ModuleDataProvider;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;
import org.eclipse.hudson.maven.model.state.BuildStateDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

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
