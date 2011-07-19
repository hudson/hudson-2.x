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

package org.eclipse.hudson.service.internal;

import static com.google.common.base.Preconditions.*;

import hudson.model.Hudson;
import hudson.model.Queue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.hudson.service.QueueService;
import org.eclipse.hudson.service.SecurityService;

/**
 * Default {@link QueueService} implementation.
 *
 * @since 2.1.0
 */
@Named
@Singleton
public class QueueServiceImpl
    extends ServiceSupport
    implements QueueService
{
    private final SecurityService security;

    @Inject
    QueueServiceImpl(final SecurityService securityService) {
        this.security = checkNotNull(securityService);
    }

    public Queue getQueue() {
        this.security.checkPermission(Hudson.ADMINISTER);
        return getHudson().getQueue();
    }
}
