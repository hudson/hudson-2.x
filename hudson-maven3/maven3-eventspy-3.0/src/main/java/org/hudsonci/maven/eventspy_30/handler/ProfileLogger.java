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

package org.hudsonci.maven.eventspy_30.handler;

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
