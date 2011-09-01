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

package org.eclipse.hudson.servlets.internal;

import org.eclipse.hudson.servlets.ServletContainer;

import hudson.model.listeners.ItemListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Starts the {@link ServletContainer}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class ServletContainerStarter
    extends ItemListener
{
    private static final Logger log = LoggerFactory.getLogger(ServletContainerStarter.class);

    // FIXME: Use of ItemListener for server life-cycle bits is a work around

    private final ServletContainer servlets;

    @Inject
    public ServletContainerStarter(final ServletContainer servlets) {
        this.servlets = checkNotNull(servlets);
    }

    @Override
    public void onLoaded() {
        try {
            servlets.start();
        }
        catch (Exception e) {
            log.error("Failed to start servlet container", e);
        }
    }
}
