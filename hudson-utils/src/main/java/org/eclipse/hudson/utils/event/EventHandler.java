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
 * Abstraction to allow event handling logic to be componentized.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public interface EventHandler<ContextType, EventType>
{
    /**
     * Called to initialize the handler w/context.
     */
    void init(ContextType context);

    /**
     * Returns true if this handler can consume the given event.
     */
    boolean accept(Object event);

    /**
     * Handles the given event.
     */
    void handle(EventType event) throws Exception;
}
