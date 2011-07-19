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

import javax.inject.Named;

/**
 * Records invocations on {@link Callback}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
public class RecordingEventSpy
    extends EventSpySupport
{
    // TODO: Setup the basic event processing bits similar to RemotingEventSpy and install a dummy callback proxy + recording handler
    // TODO: Need some more request/response methods on Callback before this can be fully validated to work

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
