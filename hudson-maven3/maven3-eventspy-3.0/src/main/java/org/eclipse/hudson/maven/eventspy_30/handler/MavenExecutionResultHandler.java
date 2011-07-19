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

import org.apache.maven.execution.MavenExecutionResult;
import org.eclipse.hudson.maven.eventspy_30.EventSpyHandler;
import org.eclipse.hudson.maven.eventspy_30.MavenProjectConverter;

import javax.inject.Named;

/**
 * Handles {@link MavenExecutionResult} events.
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
public class MavenExecutionResultHandler
    extends EventSpyHandler<MavenExecutionResult>
{
    public void handle( final MavenExecutionResult event )
        throws Exception
    {
        log.debug( "Execution result: {}", event );

        if ( event.hasExceptions() )
        {
            log.info( "Build failed with exception(s)" );

            int i = 0;
            for ( Throwable cause : event.getExceptions() )
            {
                log.info( "[{}] {}", ++i, cause );
            }
        }

        log.debug( "Recording MavenProjects" );
        getBuildRecorder().recordSessionFinished( MavenProjectConverter.extractFrom( event ) );
    }
}
