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

package org.eclipse.hudson.utils.event;

/**
 * Manages dispatching events to appropriate {@link EventHandler} instances.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public interface EventProcessor<ContextType>
{
    /**
     * Initializes all {@link EventHandler} instances.
     */
    void init(ContextType context);

    /**
     * Delegates to registered {@link EventHandler} which accepts the event, or the default handler if non accept.
     */
    void process(Object event) throws Exception;
}
