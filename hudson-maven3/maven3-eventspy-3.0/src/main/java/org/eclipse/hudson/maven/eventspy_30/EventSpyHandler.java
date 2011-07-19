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

package org.eclipse.hudson.maven.eventspy_30;

import org.eclipse.hudson.maven.eventspy_30.recorder.BuildRecorder;
import org.eclipse.hudson.utils.event.EventHandlerSupport;
import org.model.hudson.maven.eventspy.common.Callback;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link org.apache.maven.eventspy.EventSpy} event handlers.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class EventSpyHandler<T>
    extends EventHandlerSupport<EventSpyHandler.HandlerContext,T>
{
    public static class HandlerContext
    {
        private final Callback callback;
        private final BuildRecorder buildRecorder;

        public HandlerContext(final Callback callback) {
            this.callback = checkNotNull(callback);
            // TODO: how to inject since this is likely to be eventspy version specific
            buildRecorder = new BuildRecorder(callback);
        }

        public Callback getCallback() {
            return callback;
        }
        
        public BuildRecorder getBuildRecorder() {
            return buildRecorder;
        }
    }

    protected Callback getCallback() {
        return getContext().getCallback();
    }
    
    protected BuildRecorder getBuildRecorder() {
        return getContext().getBuildRecorder();
    }
}
