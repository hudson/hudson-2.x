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

import org.sonatype.gossip.support.DC;
import org.hudsonci.utils.event.EventProcessorSupport;
import org.apache.maven.BuildAbort;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link org.apache.maven.eventspy.EventSpy} event processor.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class EventSpyProcessor
    extends EventProcessorSupport<EventSpyHandler.HandlerContext,EventSpyHandler>
{
    public EventSpyProcessor(final List<EventSpyHandler> handlers, EventSpyHandler defaultHandler) {
        super(handlers, defaultHandler);
    }

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void process(final Object event) throws Exception {
        DC.put(EventSpyProcessor.class, counter.getAndIncrement());

        try {
            super.process(event);
        }
        finally {
            DC.remove(EventSpyProcessor.class);
        }
    }

    @Override
    protected void onFailure(final Exception cause) throws Exception {
        log.error("Processing failure; aborting Maven", cause);

        // FIXME: We should tell the Callback that we are aborting so it can more gracefully handle this, ATM it will look like an I/O failure when the process dies.

        throw new BuildAbort("Error occurred while processing events; aborting", cause);
    }
}
