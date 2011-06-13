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

package org.hudsonci.events.internal;

import org.hudsonci.events.EventConsumer;
import org.hudsonci.events.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.EventObject;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link EventPublisher} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class EventPublisherImpl
    implements EventPublisher
{
    private static final Logger log = LoggerFactory.getLogger(EventPublisherImpl.class);

    private final List<EventConsumer> consumers;

    @Inject
    public EventPublisherImpl(final List<EventConsumer> consumers) {
        this.consumers = checkNotNull(consumers);
    }

    private EventConsumer[] getConsumers() {
        return consumers.toArray(new EventConsumer[consumers.size()]);
    }

    public void publish(final EventObject event) {
        checkNotNull(event);

        log.trace("Publishing event: {}", event);

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();

        for (EventConsumer target : getConsumers()) {
            log.trace("Firing event ({}) to consumer: {}", event, target);

            Thread.currentThread().setContextClassLoader(target.getClass().getClassLoader());

            try {
                target.consume(event);
            }
            catch (Exception e) {
                log.error("Consumer raised an exception", e);
            }
            finally {
                Thread.currentThread().setContextClassLoader(cl);
            }
        }
    }
}
