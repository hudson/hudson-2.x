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

package org.hudsonci.maven.plugin.ui.gwt.buildinfo;

import com.google.gwt.view.client.HasData;
import org.hudsonci.maven.model.state.ArtifactDTO;
import org.hudsonci.maven.model.state.BuildStateDTO;
import org.hudsonci.maven.model.state.MavenProjectDTO;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.BuildSummaryPresenter;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.MavenBuildInfoController;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.ModuleInfoPickerPresenter;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.ModuleInfoPresenter;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.event.BuildStateSelectedEvent;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.ArtifactDataProvider;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.ModuleDataProvider;
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
