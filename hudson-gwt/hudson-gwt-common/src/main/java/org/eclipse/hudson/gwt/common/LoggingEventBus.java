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

package org.eclipse.hudson.gwt.common;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.SimpleEventBus;

import javax.inject.Singleton;

/**
 * Logging {@link SimpleEventBus}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Singleton
public class LoggingEventBus
    extends SimpleEventBus
{
    @Override
    public void fireEvent(final GwtEvent<?> event) {
        if (Log.isDebugEnabled()) {
            Log.debug("Firing event: " + event);
        }
        super.fireEvent(event);
    }

    @Override
    public void fireEventFromSource(final GwtEvent<?> event, final Object source) {
        if (Log.isDebugEnabled()) {
            Log.debug("Firing event: " + event + ", source: " + source);
        }
        super.fireEventFromSource(event, source);
    }
}
