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

import org.apache.maven.project.DependencyResolutionRequest;
import org.eclipse.hudson.maven.eventspy_30.EventSpyHandler;

import javax.inject.Named;

/**
 * Handles {@link DependencyResolutionRequest}.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Named
public class DependencyResolutionRequestHandler
    extends EventSpyHandler<DependencyResolutionRequest>
{
    public void handle(DependencyResolutionRequest event) throws Exception {
        log.debug("DependencyResolution request: {}", event);

        getBuildRecorder().recordDependencyResolutionStarted(event.getMavenProject());
    }
}
