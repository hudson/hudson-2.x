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

package org.hudsonci.maven.eventspy_30;

import org.hudsonci.maven.model.state.BuildResultDTO;
import org.hudsonci.maven.model.state.BuildSummaryDTO;
import org.hudsonci.maven.model.state.MavenProjectDTO;

import org.apache.maven.execution.BuildFailure;
import org.apache.maven.execution.BuildSuccess;
import org.apache.maven.execution.BuildSummary;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.project.MavenProject;
import org.hudsonci.maven.eventspy_30.MavenProjectConverter;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * Model to DTO conversion tests for {@link MavenProjectConverter}.
 * 
 * @author Jamie Whitehouse
 */
public class MavenProjectConverterTest
{
    @Test
    public void verifyBuildSummaryToBuildResultConversion()
    {
        BuildResultDTO success =
            MavenProjectConverter.convertToBuildResult( new BuildSuccess( createProjectStub(), 0 ) );
        assertThat( success, equalTo( BuildResultDTO.SUCCESS ) );

        BuildResultDTO failure =
            MavenProjectConverter.convertToBuildResult( new BuildFailure( createProjectStub(), 0, null ) );
        assertThat( failure, equalTo( BuildResultDTO.FAILURE ) );

        BuildResultDTO nullSummary = MavenProjectConverter.convertToBuildResult( null );
        assertThat( nullSummary, equalTo( BuildResultDTO.SKIPPED ) );

        BuildResultDTO unknownType =
            MavenProjectConverter.convertToBuildResult( new BuildSummary( createProjectStub(), 0 )
            {
            } );
        assertThat( unknownType, equalTo( BuildResultDTO.UNKNOWN ) );
    }

    @Test
    public void verifyNonNullBuildSummaryConversion()
    {
        long duration = 1002003;
        BuildSummary summary = new BuildSummary( createProjectStub(), duration )
        {
        };

        BuildSummaryDTO summaryDTO = MavenProjectConverter.convertBuildSummary( summary );
        assertThat( summaryDTO.getDuration(), equalTo( duration ) );
        // Result conversion tested elsewhere; just ensure that it's been copied into the DTO.
        assertThat( summaryDTO.getResult(), instanceOf( BuildResultDTO.class ) );

        // TODO: if BuildSummary is instance of BuildFailure there's an exception that can be added
    }

    @Test
    public void failureBuildSummaryCauseIsConverted()
    {
        BuildSummary summary = new BuildFailure( createProjectStub(), 0,
                                                 new LifecycleExecutionException( new IllegalStateException( "Nested exception for testing." ) ) );
        BuildSummaryDTO summaryDTO = MavenProjectConverter.convertBuildSummary( summary );

        // Content doesn't matter for this test.
        assertThat( summaryDTO.getFailureCause(), notNullValue() );
    }

    @Test
    public void failureBuildSummaryWithNullCauseIsConvertedWithoutNPE()
    {
        BuildSummary summary = new BuildFailure( createProjectStub(), 0, null );
        BuildSummaryDTO summaryDTO = MavenProjectConverter.convertBuildSummary( summary );

        assertThat( summaryDTO.getFailureCause(), nullValue() );
    }

    @Test
    public void nullBuildSummaryIsConvertedToSkipped()
    {
        BuildSummaryDTO summaryDTO = MavenProjectConverter.convertBuildSummary( null );
        assertThat( summaryDTO.getResult(), equalTo( BuildResultDTO.SKIPPED ) );
    }

    @Test
    public void projectUpdatedWithConvertedBuildSummary()
    {
        // BuildSummary conversion is tested elsewhere; just ensure that there is
        // an association made between the converted value and the MavenProjectDTO.

        // Expected initial state.
        MavenProjectDTO projectDTO = new MavenProjectDTO();
        assertThat( projectDTO.getBuildSummary(), nullValue() );

        BuildSummary summary = new BuildSummary( createProjectStub(), 0 )
        {
        };

        MavenProjectConverter.updateWithBuildSummary( projectDTO, summary );
        BuildSummaryDTO summaryDTO = projectDTO.getBuildSummary();
        assertThat( summaryDTO, notNullValue() );
        assertThat( summaryDTO, instanceOf( BuildSummaryDTO.class ) );
    }

    /**
     * Verifies that when {@link MavenExecutionResult#getBuildSummary(MavenProject)} returns null the project is updated
     * appropriately.
     */
    @Test
    public void projectUpdatedToSkippedWhenBuildSummaryIsNull()
    {
        MavenProjectDTO projectDTO = new MavenProjectDTO();
        MavenProjectConverter.updateWithBuildSummary( projectDTO, null );
        assertThat( projectDTO.getBuildSummary().getResult(), equalTo( BuildResultDTO.SKIPPED ) );
    }

    @Test
    public void projectWithNoBuildSummaryIsUpdatedWithConvertedBuildResult()
    {
        MavenProjectDTO projectDTO = new MavenProjectDTO();

        // test when no build summary
        MavenProjectConverter.updateWithBuildResult( projectDTO, BuildResultDTO.SCHEDULED );
        assertThat( projectDTO.getBuildSummary().getResult(), equalTo( BuildResultDTO.SCHEDULED ) );
    }

    @Test
    public void projectWithExistingBuildSummaryIsOverwrittenWithConvertedBuildResult()
    {
        MavenProjectDTO projectDTO = new MavenProjectDTO();
        projectDTO.setBuildSummary( new BuildSummaryDTO().withResult( BuildResultDTO.SUCCESS ).withDuration( 500L ) );
        assertThat( projectDTO.getBuildSummary(), notNullValue() );

        BuildSummary summary = new BuildFailure( createProjectStub(), 1500, null );

        // test when build summary already exists
        MavenProjectConverter.updateWithBuildSummary( projectDTO, summary );
        assertThat( projectDTO.getBuildSummary().getResult(), equalTo( BuildResultDTO.FAILURE ) );
        assertThat( projectDTO.getBuildSummary().getDuration(), equalTo( 1500l ) );
    }

    @Test
    public void verifyMavenProjectBasicConversion()
    {
        MavenProject project = createProjectStub();
        MavenProjectDTO projectDTO = MavenProjectConverter.convertMavenProject( project );
        assertThat( projectDTO.getName(), equalTo( project.getName() ) );
        assertThat( projectDTO.getId(), equalTo( "org.hudsonci.example:example-project:jar:1.0-SNAPSHOT") );
        
        // TODO: no name set
    }

    private MavenProject createProjectStub()
    {
        MavenProject project = new MavenProject();
        project.setName( "Project for testing" );
        project.setGroupId( "org.hudsonci.example" );
        project.setArtifactId( "example-project" );
        project.setVersion( "1.0-SNAPSHOT" );
        return project;
    }
}
