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

import org.hudsonci.maven.model.state.MavenProjectDTO;

import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.project.MavenProject;
import org.hudsonci.maven.eventspy.common.Callback;
import org.hudsonci.maven.eventspy_30.EventSpyHandler;
import org.hudsonci.maven.eventspy_30.handler.MavenExecutionResultHandler;
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
