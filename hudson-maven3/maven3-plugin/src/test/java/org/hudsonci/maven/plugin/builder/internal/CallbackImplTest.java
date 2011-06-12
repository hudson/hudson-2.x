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

package org.hudsonci.maven.plugin.builder.internal;

import org.hudsonci.maven.eventspy.common.Callback;
import org.hudsonci.maven.model.state.BuildStateDTO;
import org.hudsonci.maven.model.state.MavenProjectDTO;
import org.hudsonci.maven.model.state.RuntimeEnvironmentDTO;

import org.hamcrest.Matchers;
import org.hudsonci.maven.plugin.builder.internal.CallbackImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hudsonci.maven.model.test.CannedDtos.fakeCoordinates;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tests for {@link CallbackImpl}.
 * 
 * @author Jamie Whitehouse
 */
public class CallbackImplTest
{
    private BuildStateDTO buildState;

    private Callback callback;

    @Before
    public void configureCallback()
    {
        buildState = new BuildStateDTO();
        assertThat( buildState.getRuntimeEnvironment(), nullValue() );
        assertThat( buildState.getParticipatingProjects(), Matchers.<MavenProjectDTO> empty() );
        
        callback = new CallbackImpl( buildState );
    }

    @Test
    public void runtimeEnvironmentPassedToBuildState()
    {
        RuntimeEnvironmentDTO env = new RuntimeEnvironmentDTO();
        callback.setRuntimeEnvironment( env );
        assertThat( buildState.getRuntimeEnvironment(), equalTo( env ) );
    }

    @Test
    public void nullProjectsInBuildStateIsEmptyList()
    {
        BuildStateDTO buildState = new BuildStateDTO();
        assertThat( buildState.getParticipatingProjects(), notNullValue() );
        assertThat( buildState.getParticipatingProjects(), Matchers.<MavenProjectDTO> empty() );
    }

    @Test
    public void setProjectsPassedToBuildState()
    {
        List<MavenProjectDTO> projects = Arrays.asList( new MavenProjectDTO() );
        callback.setParticipatingProjects( projects );
        assertThat( buildState.getParticipatingProjects(), equalTo( projects ) );
    }

    @Test
    public void setProjectsReplaceExistingCollection() {
        callback.setParticipatingProjects( Arrays.asList( new MavenProjectDTO() ) );
        assertThat( buildState.getParticipatingProjects(), hasSize( 1 ) );

        callback.setParticipatingProjects( Arrays.asList( new MavenProjectDTO() ) );
        assertThat( buildState.getParticipatingProjects(), hasSize( 1 ) );
    }
    
    @Test
    public void updatedProjectPassedToBuildState()
    {
        fakeCoordinates("one");
        
        MavenProjectDTO originalProject = new MavenProjectDTO().withCoordinates( fakeCoordinates("one") );
        callback.setParticipatingProjects( Arrays.asList( originalProject ) );

        // Make a new object to ensure object instance is not interfering with comparison.
        MavenProjectDTO updatedProject = new MavenProjectDTO().withCoordinates( fakeCoordinates("one") ).withName( "updated" );
        callback.updateParticipatingProject( updatedProject );

        assertThat( buildState.getParticipatingProjects(), contains( updatedProject ) );
    }

    @Test
    public void unmatchedProjectIsNotUpdatedOrAdded()
    {
        callback.setParticipatingProjects( Arrays.asList( new MavenProjectDTO().withCoordinates( fakeCoordinates("one") ),
                                                          new MavenProjectDTO().withCoordinates( fakeCoordinates("two") ) ) );

        MavenProjectDTO unmatchedProject = new MavenProjectDTO().withCoordinates( fakeCoordinates("project-not-yet-set") );
        callback.updateParticipatingProject( unmatchedProject );

        assertThat( buildState.getParticipatingProjects(), not( contains( unmatchedProject ) ) );
    }
}
