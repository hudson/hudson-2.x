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

import javax.inject.Named;

import org.eclipse.hudson.maven.eventspy_30.EventSpyHandler;
import org.eclipse.hudson.maven.eventspy_30.LifecycleDebugLoggerImpl.WeavePlanEvent;

/**
 * Handles {@link WeavePlanEvent} events.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
public class WeavePlanEventHandler
    extends EventSpyHandler<WeavePlanEvent>
{
    public void handle(final WeavePlanEvent event) throws Exception {
        log.debug("Weave plan: {}", event);
    }
}                                          
