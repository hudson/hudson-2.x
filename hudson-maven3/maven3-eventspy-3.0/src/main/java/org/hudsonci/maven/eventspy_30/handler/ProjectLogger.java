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
