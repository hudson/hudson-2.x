/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30.handler;

import com.sonatype.matrix.maven.eventspy_30.EventSpyHandler;

import org.apache.maven.project.DependencyResolutionResult;

import javax.inject.Named;

/**
 * Handles {@link DependencyResolutionResult}.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
@Named
public class DependencyResolutionResultHandler
    extends EventSpyHandler<DependencyResolutionResult>
{
    @Override
    public void handle(DependencyResolutionResult event) throws Exception {
        log.debug("DependencyResolution result: {}", event);

        getBuildRecorder().recordDependencyResolutionFinished(event.getResolvedDependencies());
    }
}
