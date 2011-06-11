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

import org.hudsonci.utils.common.TestAccessible;
import org.hudsonci.maven.model.state.ArtifactDTO;
import org.hudsonci.maven.model.state.ArtifactOperationDTO;

import org.hudsonci.maven.eventspy_30.EventSpyHandler;
import org.hudsonci.maven.eventspy_30.MavenArtifactConverter;
import org.sonatype.aether.RepositoryEvent;
import org.sonatype.aether.RepositoryEvent.EventType;
import org.sonatype.aether.RequestTrace;
import org.sonatype.aether.repository.ArtifactRepository;
import org.sonatype.aether.repository.RemoteRepository;

import javax.inject.Named;

import java.util.Arrays;
import java.util.List;

import static org.sonatype.aether.RepositoryEvent.EventType.ARTIFACT_DEPLOYED;
import static org.sonatype.aether.RepositoryEvent.EventType.ARTIFACT_DOWNLOADED;
import static org.sonatype.aether.RepositoryEvent.EventType.ARTIFACT_INSTALLED;
import static org.sonatype.aether.RepositoryEvent.EventType.ARTIFACT_RESOLVED;

/**
 * Handles {@link RepositoryEvent} events.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Jamie Whitehouse
 * 
 * @since 2.1.0
 */
@Named
public class RepositoryEventHandler
    extends EventSpyHandler<RepositoryEvent>
{
    /**
     * ARTIFACT_DOWNLOADED ; it's been downloaded
     * ARTIFACT_RESOLVED; it's been resolved, either downloaded or cached
     * ARTIFACT_INSTALLED
     * ARTIFACT_DEPLOYED
     */
    private static final List<EventType> interestingArtifactEvents = Arrays.asList( ARTIFACT_RESOLVED, ARTIFACT_DOWNLOADED, ARTIFACT_INSTALLED, ARTIFACT_DEPLOYED );

    public void handle(final RepositoryEvent event) throws Exception {
        log.debug("Repository event: {}", event);
        
        logRequestTrace(event);
        
        if( isInterestingArtifactEvent( event ) )
        {
            // Converter checks for nulls.
            ArtifactDTO artifact = MavenArtifactConverter.convertAetherArtifact(event.getArtifact());

            // getVersion vs getBaseVersion
            // getVersion is the timestamped snapshot vs -SNAPSHOT
            // Record both, MavenCoordinatesDTO.getVersion() should always return -SNAPSHOT for 
            // snapshot artifacts, unless the pom is explicitly using a timestamp snapshot.
            // * INSTALLED vs DEPLOYED ramifications:
            // use of this as the version would cause deployed artifacts to not match any 
            // registered installed artifacts because installed artifacts are -SNAPSHOT
            // * External -SNAPSHOT dependency DOWNLOADED and RESOLVED ramifications:
            // the aether getVersion is the timestamped format, which is handy to know the exact version used in the build
            // but if -SNAPSHOT was declared in the pom then the devs don't likely care that much.
            // TODO: Could also impact up/downstream triggered builds and fingerprinting?

            ArtifactOperationDTO operation = resolveOperationType( event );
            // Null File is acceptable.
            getBuildRecorder().recordArtifactAction(artifact, operation, event.getFile());
            
            //logRepositoryConfiguration(operation,event);
        }
    }
    
    /**
     * Capture deployed artifacts remote repo config for later use in uploading the buildinfo for the artifact.
     */
    private void logRepositoryConfiguration(final ArtifactOperationDTO operation, final RepositoryEvent event) {
        if (event.getRepository() != null) {
            log.debug("Repo type: {}; info {}", event.getRepository().getClass(), event.getRepository());
        }

        if (ArtifactOperationDTO.DEPLOYED.equals(operation)) {
            ArtifactRepository repository = event.getRepository();
            if (repository instanceof RemoteRepository) {
                RemoteRepository remoteRepo = (RemoteRepository) repository;
                log.debug("Deploy info for remote repository: {}", repository);
                log.debug("    Authentication: {}", remoteRepo.getAuthentication());
            }
        }
    }

    /**
     * From Benjamin Bentmann:
     * Aether artifact resolution can succeed or fail, the event is fired in 
     * both cases to signal the end of the resolution.
     * Checking the artifact file is another way to detect failure/success, by 
     * definition, an artifact is considered resolved if and only if it has a file.
     */
    @TestAccessible
    ArtifactOperationDTO resolveOperationType( final RepositoryEvent event )
    {
        ArtifactOperationDTO result;
        if( null == event.getFile() )
        {
            for( Exception e : event.getExceptions() )
            {
                log.debug( "Artifact not found", e );
            }

            result = ArtifactOperationDTO.NOT_FOUND;
        }
        else
        {
            result = ArtifactOperationDTO.valueOf( event.getType().name().substring( 9 ) ); 
        }
        
        return result;
    }

    // TODO: find a better name, something to indicate that it's about actions
    // being completed e.g. INSTALLED not INSTALLING
    private boolean isInterestingArtifactEvent( final RepositoryEvent event )
    {
        return interestingArtifactEvents.contains( event.getType() );
    }
    
    private void logRequestTrace(final RepositoryEvent event) {
        // Use trace level logging because this is a lot of information that
        // we're currently not using.
        if (!log.isTraceEnabled()) {
            // Don't compute trace logging if we're not logging.
            return;
        }

        RequestTrace currentNode = event.getTrace();

        if (currentNode == null) {
            // Nothing to log.
            return;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(currentNode.getData().getClass().getSimpleName());

        while ((currentNode = currentNode.getParent()) != null) {
            sb.append(" <= ");
            sb.append(currentNode.getData().getClass().getSimpleName());
        }

        log.trace("Trace stack: {}; value {}", sb.toString(), event.getTrace().getData());
    }
}
