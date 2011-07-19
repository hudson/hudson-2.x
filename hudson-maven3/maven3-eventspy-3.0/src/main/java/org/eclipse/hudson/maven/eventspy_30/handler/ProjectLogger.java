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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.PluginArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class ProjectLogger
{
    private static final Logger log = LoggerFactory.getLogger(ProjectLogger.class);
    private static final boolean disabled = !Boolean.valueOf(System.getProperty("hudson.eventspy.logging.project"));
    
    public static void log( MavenProject project, String where )
    {
        if( disabled ) return;
        
        log.debug( "MavenProject ({}) artifacts @ {}:", project.getId(), where);
        logArtifactContents( "artifacts", project.getArtifacts() );
        logArtifactContents( "attachedArtifacts", project.getAttachedArtifacts() );
        logArtifactContents( "dependencyArtifacts", project.getDependencyArtifacts() );
        logArtifactContents( "extensionArtifacts", project.getExtensionArtifacts() );
        logArtifactContents( "pluginArtifacts", project.getPluginArtifacts() );
        
        for( Artifact artifact : project.getPluginArtifacts() )
        {
            if (artifact instanceof PluginArtifact) {
                List<Dependency> dependencies = ((PluginArtifact) artifact).getDependencies();
                
                Integer maybeSize = (dependencies == null ? null : dependencies.size());
                log.debug( "  {} " + "pluginDependencies" + ": {}", maybeSize, dependencies );
            }
        }
    }

    private static void logArtifactContents( String method, Collection<? extends Artifact> artifacts )
    {
        Integer maybeSize = (artifacts == null ? null : artifacts.size());
        log.debug( "  {} " + method + ": {}", maybeSize, artifacts );
    }
}
