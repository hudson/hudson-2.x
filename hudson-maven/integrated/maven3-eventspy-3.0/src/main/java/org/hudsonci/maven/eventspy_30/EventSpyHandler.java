/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.eventspy_30;

import org.hudsonci.maven.eventspy.common.Callback;
import org.hudsonci.maven.eventspy_30.recorder.BuildRecorder;

import org.hudsonci.utils.event.EventHandlerSupport;

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
