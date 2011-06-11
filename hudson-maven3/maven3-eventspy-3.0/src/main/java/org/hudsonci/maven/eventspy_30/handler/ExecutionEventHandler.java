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

import org.hudsonci.maven.model.MavenCoordinatesDTO;
import org.hudsonci.maven.model.state.ArtifactDTO;
import org.hudsonci.maven.model.state.MavenProjectDTO;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.BuildSummary;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionEvent.Type;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.hudsonci.maven.eventspy_30.EventSpyHandler;

import javax.inject.Named;

import java.util.List;
import java.util.Set;

import static org.apache.maven.execution.ExecutionEvent.Type.ProjectFailed;
import static org.apache.maven.execution.ExecutionEvent.Type.ProjectSkipped;
import static org.apache.maven.execution.ExecutionEvent.Type.ProjectStarted;
import static org.apache.maven.execution.ExecutionEvent.Type.ProjectSucceeded;
import static org.apache.maven.execution.ExecutionEvent.Type.SessionStarted;

/**
 * Handles {@link ExecutionEvent} events.
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Named
public class ExecutionEventHandler
    extends EventSpyHandler<ExecutionEvent>
{
    public void handle( final ExecutionEvent event ) throws Exception
    {
        Type type = event.getType();
        log.debug( "Execution event type: {}", type );

        recordSessionStarted( event );
        recordProjectStarted( event );
        recordMojoStarted( event );
        recordProjectFinished( event );
        // TODO: could probably handle SessionEnded instead of MavenExecutionResult
        // in MavenExecutionResultHandler
    }

    private void recordSessionStarted( final ExecutionEvent event )
    {
        if ( SessionStarted.equals( event.getType() ) )
        {
            List<MavenProject> projects = event.getSession().getProjects();

            log.debug( "Recording MavenProjects" );
            getBuildRecorder().recordSessionStarted(projects);
            
            ProfileLogger.log( event ); // TODO: is this needed anymore?
        }
    }

    private void recordProjectStarted( final ExecutionEvent event )
    {
        if ( ProjectStarted.equals( event.getType() ) )
        {
            MavenProject project = event.getProject();
            
            log.debug( "Updating MavenProject" );
            getBuildRecorder().recordProjectStarted(project);

            ProjectLogger.log( project, "project started" );
        }
    }

    private void recordProjectFinished( final ExecutionEvent event )
    {
        Type type = event.getType();
        if ( ProjectSucceeded.equals( type ) || ProjectFailed.equals( type ) || ProjectSkipped.equals( type ) )
        {
            MavenProject project = event.getProject();
            BuildSummary buildSummary = event.getSession().getResult().getBuildSummary( project );

            log.debug( "Updating MavenProject" );
            getBuildRecorder().recordProjectFinished(project, buildSummary);
            
            // Record artifact usage at end of project build since they are 
            // populated in each phase, not at the beginning.
            // NOTE: Benjamin has indicated that this is very closely bound to 
            // the plugin lifecycle and exposes only those artifacts, that a 
            // plugin has asked for now.
            // Hence why there's no info at project finished.
            ProjectLogger.log( project, "project finished" );
        }
    }
    
    private void recordMojoStarted( final ExecutionEvent event )
    {
        if( ExecutionEvent.Type.MojoStarted.equals(event.getType()) )
        {
            MojoExecution mojoExecution = event.getMojoExecution();
            ProjectLogger.log(event.getProject(), "mojo started - " + mojoExecution.getLifecyclePhase() + " " + mojoExecution.getArtifactId() + " " + mojoExecution.getExecutionId() );
            
            // There are none.
            //log.debug("Mojo Plugin deps: {}", mojoExecution.getPlugin().getDependencies());
        }
    }

    // TODO: Use this information to verify that repo events are being
    // properly correlated to the current executing project.
    // This is not an exhaustive list, but they all should be matched as
    // a minimal test. 
    // Or use the info from ProjectLogger.
    @SuppressWarnings( "unused" )
    private void recordDirectArtifacts( MavenProject project, MavenProjectDTO projectDTO )
    {
        Set<Artifact> artifacts = project.getDependencyArtifacts();
        // TODO: may want to record these separately as plugin use.
        // Note:
        //    project.getPluginArtifacts() makes the 'type/extension' maven-plugin which doesn't match anything Aether has resolved.
        //    see underlying org.apache.maven.artifact.factory.DefaultArtifactFactory.createPluginArtifact()
        //    With m3 use Aether Artifact.getProperty( "type" ); see it's JavaDoc
        //artifacts.addAll( project.getPluginArtifacts() );

        for( Artifact artifact : artifacts )
        {
            ArtifactDTO artifactDTO = new ArtifactDTO().withCoordinates( new MavenCoordinatesDTO()
                .withGroupId( artifact.getGroupId() )
                .withArtifactId( artifact.getArtifactId() )
                .withType( artifact.getType() )
                .withVersion( artifact.getVersion() )
                .withClassifier( artifact.getClassifier() )
                .normalize() );
            
            artifactDTO.getDependentProjects().add( projectDTO.getId() );
            // TODO: Maybe do in batch since it's traffic back to the master.
            //getCallback().updateArtifact( artifactDTO );
        }
    }
}
