/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30.handler;

import com.sonatype.matrix.maven.eventspy_30.EventSpyHandler;
import com.sonatype.matrix.maven.eventspy_30.LifecycleDebugLoggerImpl.ProjectPlanEvent;

import javax.inject.Named;

/**
 * Handles {@link ProjectPlanEvent} events.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Named
public class ProjectPlanEventHandler
    extends EventSpyHandler<ProjectPlanEvent>
{
    public void handle(final ProjectPlanEvent event) throws Exception {
        log.debug("Project plan: {}", event);
    }
}