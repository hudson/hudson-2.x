/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30.handler;

import com.sonatype.matrix.maven.eventspy.common.Callback;
import com.sonatype.matrix.maven.eventspy_30.EventSpyHandler;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;

import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith( MockitoJUnitRunner.class )
public class MavenExecutionResultHandlerTest
{
    @Mock
    private Callback callback;
    private EventSpyHandler<MavenExecutionResult> eventHandler;

    @Before
    public void configureHandler()
    {
        // add the handlers that I'm interested in...
        // plexus wires this up for us
        // MavenExecutionResultHandler eventHandler = new MavenExecutionResultHandler();
        // EventSpyProcessor processor = new EventSpyProcessor(null, eventHandler);
        // processor.init(new EventSpyHandler.HandlerContext(mock( Callback.class )));
        // processor.process( eventHandler )
        // eventHandler.handle( mock(MavenExecutionResult.class) );

        eventHandler = new MavenExecutionResultHandler();
        eventHandler.init( new EventSpyHandler.HandlerContext( callback ) );
    }
    
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Test
    public void participatingProjectsAreCollected()
        throws Exception
    {
        MavenExecutionResult resultWithProjects = new DefaultMavenExecutionResult();
        resultWithProjects.setTopologicallySortedProjects( Arrays.asList( new MavenProject[] 
        {
            new MavenProject(),
            new MavenProject(),
            new MavenProject()
        }) );

        eventHandler.handle( resultWithProjects );

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass( List.class );
        verify( callback, times( 1 ) ).setParticipatingProjects( captor.capture() );

        List<MavenProjectDTO> dtos = captor.getValue();
        assertThat( dtos, hasSize( 3 ) );
        
        // Assume that all translation work is done by the MavenProjectConverter,
        // which is tested elsewhere.
        // Hence, no need to check the collection contents.
    }
    
    @Test
    public void nullProjectsDoNotThrowExceptions() throws Exception
    {
        // Ensures that we're using a Maven version that has MNG-4904 applied.
        MavenExecutionResult result = new DefaultMavenExecutionResult();
        result.setTopologicallySortedProjects( null );
        
        eventHandler.handle( result );
    }
}
