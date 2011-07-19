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

package org.eclipse.hudson.maven.eventspy_30.handler;

import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map.Entry;

public class ProfileLogger
{
    private static final Logger log = LoggerFactory.getLogger(ProfileLogger.class);
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
    public static void logRequestProfiles( final MavenExecutionRequest event )
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
