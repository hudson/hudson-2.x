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

package org.eclipse.hudson.maven.eventspy_30;

import org.eclipse.hudson.maven.model.ThrowableDTOHelper;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import org.eclipse.hudson.maven.model.state.BuildResultDTO;
import org.eclipse.hudson.maven.model.state.BuildSummaryDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;
import org.eclipse.hudson.maven.model.state.ProfileDTO;

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
