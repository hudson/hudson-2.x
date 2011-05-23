/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30;

import org.sonatype.gossip.support.DC;
import com.sonatype.matrix.common.event.EventProcessorSupport;
import org.apache.maven.BuildAbort;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link org.apache.maven.eventspy.EventSpy} event processor.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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