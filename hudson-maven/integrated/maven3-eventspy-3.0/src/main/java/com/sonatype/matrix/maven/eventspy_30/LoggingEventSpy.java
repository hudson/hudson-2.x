/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy_30;

import javax.inject.Named;

/**
 * A simple {@link org.apache.maven.eventspy.EventSpy} which simply logs method calls.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Named
public class LoggingEventSpy
    extends EventSpySupport
{
    @Override
    public void init(final Context context) throws Exception {
        log.debug("init: {}", context);
    }

    @Override
    public void close() throws Exception {
        log.debug("close");
    }

    @Override
    public void onEvent(final Object event) throws Exception {
        log.debug("event: {}", event);
    }
}