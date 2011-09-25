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

package org.hudsonci.events.ready;

import org.hudsonci.events.EventPublisher;

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
                log.info("Hudson is ready.");
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
