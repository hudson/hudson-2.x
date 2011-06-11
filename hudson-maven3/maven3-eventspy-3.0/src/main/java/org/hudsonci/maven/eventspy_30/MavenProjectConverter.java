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

import org.hudsonci.maven.model.MavenCoordinatesDTO;
import org.hudsonci.maven.model.ThrowableDTOHelper;
import org.hudsonci.maven.model.state.BuildResultDTO;
import org.hudsonci.maven.model.state.BuildSummaryDTO;
import org.hudsonci.maven.model.state.MavenProjectDTO;
import org.hudsonci.maven.model.state.ProfileDTO;

import org.apache.maven.execution.BuildFailure;
import org.apache.maven.execution.BuildSuccess;
import org.apache.maven.execution.BuildSummary;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converts {@link MavenProject}s and their {@link BuildSummary}s to corresponding DTOs.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class MavenProjectConverter
{
    private MavenProjectConverter()
    {
        // non-instantiable
    }

    public static ArrayList<MavenProjectDTO> extractFrom( final MavenExecutionResult event )
    {
        ArrayList<MavenProjectDTO> participatingProjects = new ArrayList<MavenProjectDTO>();

        List<MavenProject> projects = event.getTopologicallySortedProjects();
        
        for ( MavenProject mavenProject : projects )
        {
            MavenProjectDTO projectDTO = convertMavenProject( mavenProject );
            updateWithBuildSummary( projectDTO, event.getBuildSummary( mavenProject ) );
            participatingProjects.add( projectDTO );
        }
        return participatingProjects;
    }

    public static MavenProjectDTO convertMavenProject( final MavenProject mavenProject )
    {
        checkNotNull(mavenProject);
        
        MavenProjectDTO projectDTO = new MavenProjectDTO()
            .withName( mavenProject.getName() )
            .withCoordinates( asCoordinates( mavenProject ) )
            .withProfiles( convertProfiles( ProfileCollector.collect( mavenProject ) ) );
        
        return projectDTO;
    }

    public static MavenCoordinatesDTO asCoordinates( final MavenProject mavenProject )
    {
        checkNotNull(mavenProject);
        
        // Assume groupId, artifactId and version are never null.
        return new MavenCoordinatesDTO()
            .withGroupId( mavenProject.getGroupId() )
            .withArtifactId( mavenProject.getArtifactId() )
            .withType( nullSafeString( mavenProject.getPackaging() ) )
            .withVersion( mavenProject.getVersion() )
            .normalize();
    }
    
    static private String nullSafeString( String original )
    {
        return ( original == null || original.length() == 0 ) ? "" : original;
    }

    /**
     * Add the converted build summary to the project.
     * 
     * Assumes that the project attached to the BuildSummary matches the projectDTO.
     * If the summary is null the result will be {@link BuildResultDTO#SKIPPED}.
     * This will replace any existing summary associated with the project.
     */
    public static void updateWithBuildSummary( final MavenProjectDTO projectDTO, final BuildSummary buildSummary )
    {
        checkNotNull(projectDTO);
        // BuildSummary can be null.

        projectDTO.setBuildSummary( convertBuildSummary( buildSummary ) );
    }

    /**
     * This will replace any existing summary associated with the project.
     */
    public static void updateWithBuildResult( final MavenProjectDTO projectDTO, final BuildResultDTO resultDTO )
    {
        checkNotNull(projectDTO);
        checkNotNull(resultDTO);

        projectDTO.setBuildSummary( new BuildSummaryDTO().withResult( resultDTO ) );
    }

    /**
     * If the summary is null the result will be {@link BuildResultDTO#SKIPPED}.
     */
    public static BuildSummaryDTO convertBuildSummary( final BuildSummary buildSummary )
    {
        // BuildSummary can be null.

        BuildSummaryDTO buildSummaryDTO = new BuildSummaryDTO().withResult( convertToBuildResult( buildSummary ) );

        if ( buildSummary != null )
        {
            buildSummaryDTO.setDuration( buildSummary.getTime() );

            if ( buildSummary instanceof BuildFailure )
            {
                Throwable cause = ( (BuildFailure) buildSummary ).getCause();
                if ( cause != null )
                {
                    buildSummaryDTO.setFailureCause( ThrowableDTOHelper.convert(cause) );
                }
            }
        }

        return buildSummaryDTO;
    }

    /**
     * If the summary is null the result will be {@link BuildResultDTO#SKIPPED}.
     * If a translation can not be matched the result will be {@link BuildResultDTO#UNKNOWN}.
     */
    public static BuildResultDTO convertToBuildResult( final BuildSummary buildSummary )
    {
        // BuildSummary can be null.

        final BuildResultDTO result;

        if ( buildSummary == null )
        {
            result = BuildResultDTO.SKIPPED;
        }
        else if ( buildSummary instanceof BuildSuccess )
        {
            result = BuildResultDTO.SUCCESS;
        }
        else if ( buildSummary instanceof BuildFailure )
        {
            result = BuildResultDTO.FAILURE;
        }
        else
        {
            result = BuildResultDTO.UNKNOWN;
        }

        return result;
    }
    
    public static Collection<ProfileDTO> convertProfiles( final Collection<ResolvedProfile> profiles )
    {
        checkNotNull(profiles);

        Collection<ProfileDTO> profilesDTO = new ArrayList<ProfileDTO>();

        for ( ResolvedProfile resolvedProfile : profiles )
        {
            profilesDTO.add( new ProfileDTO()
                                    .withId( resolvedProfile.getId() )
                                    .withSource( resolvedProfile.getSource() )
                                    .withActive( resolvedProfile.isActive() ) );
        }
        return profilesDTO;

    }
}
