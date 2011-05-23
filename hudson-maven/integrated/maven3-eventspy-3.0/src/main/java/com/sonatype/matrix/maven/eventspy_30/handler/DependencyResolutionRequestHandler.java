/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30.handler;

import com.sonatype.matrix.maven.eventspy_30.EventSpyHandler;

import org.apache.maven.project.DependencyResolutionRequest;

import javax.inject.Named;

/**
 * Handles {@link DependencyResolutionRequest}.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
@Named
public class DependencyResolutionRequestHandler
    extends EventSpyHandler<DependencyResolutionRequest>
{
    @Override
    public void handle(DependencyResolutionRequest event) throws Exception {
        log.debug("DependencyResolution request: {}", event);

        getBuildRecorder().recordDependencyResolutionStarted(event.getMavenProject());
    }
}
