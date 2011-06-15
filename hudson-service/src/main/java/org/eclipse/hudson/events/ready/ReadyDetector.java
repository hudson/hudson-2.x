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

package org.eclipse.hudson.events.ready;

import org.eclipse.hudson.events.EventPublisher;

import hudson.init.InitMilestone;
import hudson.model.Hudson;
import hudson.security.HudsonFilter;
import hudson.stapler.WebAppController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import java.lang.reflect.Field;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Detects when the system is ready for work and publishes a {@link ReadyEvent}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class ReadyDetector
    extends Thread
{
    private static final Logger log = LoggerFactory.getLogger(ReadyDetector.class);

    private final EventPublisher publisher;

    private final Hudson hudson;

    private final WebAppController controller;

    @Inject
    public ReadyDetector(final EventPublisher publisher, final Hudson hudson) {
        this.publisher = checkNotNull(publisher);
        this.hudson = checkNotNull(hudson);
        this.controller = WebAppController.get();
        setDaemon(true);
    }

    public void run() {
        while (true) {
            if (isReady()) {
                publisher.publish(new ReadyEvent(hudson));
                break;
            }
            else {
                try {
                    Thread.sleep(500);
                }
                catch (InterruptedException e) {
                    log.warn("Interrupted while waiting for initialization; ignoring", e);
                }
            }
        }
    }

    private boolean isReady() {
        // Hudson does not give us a nice InitMilestone when its really ready,
        // but if its not yet COMPLETED, don't bother trying to figure out more
        if (hudson.getInitLevel() != InitMilestone.COMPLETED) {
            return false;
        }

        HudsonFilter filter = HudsonFilter.get(hudson.servletContext);
        if (filter == null) {
            return false;
        }

        // Need to get access to the filter's filter field to see if its actually initialized or not
        // it does not expose it directly, so we have to use reflection to force access

        Filter delegate = getDelegate(filter);
        if (delegate == null) {
            return false;
        }

        // At this point we _should_ be ready, see if the app root object is installed... fingers crossed!
        try {
            Object app = controller.current(); // FIXME: This may actually be the only check needed?
            return app instanceof hudson.model.Hudson;
        } catch (IllegalStateException e) {
            return false; // context not yet available
        }
    }

    private Filter getDelegate(final HudsonFilter filter) {
        assert filter != null;
        try {
            Field field = filter.getClass().getDeclaredField("filter");
            field.setAccessible(true);
            return (Filter) field.get(filter);
        }
        catch (Exception e) {
            throw new Error("Failed to access HudsonFilter.filter delegate", e);
        }
    }
}
