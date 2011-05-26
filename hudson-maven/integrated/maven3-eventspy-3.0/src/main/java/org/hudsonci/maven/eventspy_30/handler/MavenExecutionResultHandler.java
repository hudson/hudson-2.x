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

import org.apache.maven.execution.MavenExecutionResult;
import org.hudsonci.maven.eventspy_30.EventSpyHandler;
import org.hudsonci.maven.eventspy_30.MavenProjectConverter;

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
