/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.internal;

import com.sonatype.matrix.maven.eventspy.common.Callback;
import com.sonatype.matrix.maven.model.state.BuildStateDTO;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;
import com.sonatype.matrix.maven.model.state.RuntimeEnvironmentDTO;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.sonatype.matrix.maven.model.test.CannedDtos.fakeCoordinates;

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
