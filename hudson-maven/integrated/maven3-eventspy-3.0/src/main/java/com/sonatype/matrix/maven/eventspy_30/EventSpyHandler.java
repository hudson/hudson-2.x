/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30;

import com.sonatype.matrix.common.event.EventHandlerSupport;
import com.sonatype.matrix.maven.eventspy.common.Callback;
import com.sonatype.matrix.maven.eventspy_30.recorder.BuildRecorder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link org.apache.maven.eventspy.EventSpy} event handlers.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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
            // TODO: how to inject since this is likely to be eventspy/matrix version specific
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