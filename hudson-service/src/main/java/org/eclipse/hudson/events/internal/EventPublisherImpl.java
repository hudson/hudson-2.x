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
import org.eclipse.hudson.events.EventPublisher;
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
