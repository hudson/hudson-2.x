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

package org.eclipse.hudson.events.internal;

import org.eclipse.hudson.events.EventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.EventObject;

/**
 * Adds diagnostic information when an event was published.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class DiagnosticEventConsumer
    implements EventConsumer
{
    private static final Logger log = LoggerFactory.getLogger(DiagnosticEventConsumer.class);

    public void consume(final EventObject event) throws Exception {
        log.trace("Event published: {}", event);
    }
}
