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

import org.eclipse.hudson.inject.Priority;

import hudson.model.listeners.ItemListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Starts the {@link ReadyDetector}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
@Priority(Integer.MIN_VALUE) // run last
public class ReadyDetectorStarter
    extends ItemListener
{
    private static final Logger log = LoggerFactory.getLogger(ReadyDetectorStarter.class);

    // FIXME: Use of ItemListener for server life-cycle bits is a work around
    private final ReadyDetector detector;

    @Inject
    public ReadyDetectorStarter(final ReadyDetector detector) {
        this.detector = checkNotNull(detector);
    }

    @Override
    public void onLoaded() {
        log.debug("Starting ready detector");
        detector.start();
    }
}
