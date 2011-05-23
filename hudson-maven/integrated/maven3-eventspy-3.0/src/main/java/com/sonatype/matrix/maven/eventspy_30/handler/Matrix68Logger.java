/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30.handler;

import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map.Entry;

public class Matrix68Logger
{
    private static final Logger log = LoggerFactory.getLogger(Matrix68Logger.class);
    private static final boolean disabled = true;

    @SuppressWarnings("unused")
    public static void log( final ExecutionEvent event )
    {
        if( disabled ) return;
        
        for (MavenProject project : event.getSession().getProjects()) {
            log.debug("*** Examining profiles for {}.", project.getName());
            logProfileList( project.getActiveProfiles(), "active" );
            logProfileList( project.getModel().getProfiles(), "model");
            
            //logProfiles( event.getSession().getProjectBuildingRequest().getProfiles(), "ProjectBuildingRequest" );
            logProfileList( project.getProjectBuildingRequest().getProfiles(), "ProjectBuildingRequest" );
            
            log.debug( "InjectedProfileIds" );
            for ( Entry<String, List<String>> entry : project.getInjectedProfileIds().entrySet() )
            {
                log.debug( "  from {} are {}", entry.getKey(), entry.getValue() );
            }

            Settings settings = event.getSession().getSettings();
            logSettingsProfileList( settings.getProfiles(), "session-settings" );
            
            log.debug( "Collected projects: {}", project.getCollectedProjects() );
            log.debug( "Project references: {}", project.getProjectReferences() );
        } 
    }

    @SuppressWarnings("unused")
    public static void logMatrix68Info( final MavenExecutionRequest event )
    {
        if( disabled ) return;
        
        log.debug( "*** Examinig Request profiles." );
        logProfileList( event.getProfiles(), "profiles" );
        log.debug( "   active {}", event.getActiveProfiles() );
        log.debug( "   inactive {}", event.getInactiveProfiles() );
    }
    
    private static void logProfileList( List<Profile> profiles, String type )
    {
        log.debug( String.format( "%s %s profiles.", type, profiles.size() ) );
        for ( Profile profile : profiles )
        {
            log.debug( "  {}", profile );
        }
    }
    
    private static void logSettingsProfileList( List<org.apache.maven.settings.Profile> profiles, String type )
    {
        log.debug( String.format( "%s %s profiles.", type, profiles.size() ) );
        for ( org.apache.maven.settings.Profile profile : profiles )
        {
            log.debug( "  {}", String.format("Profile {id: %s, source: %s}", profile.getId(), profile.getSourceLevel() ) );
        }
    }
}
