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

package org.eclipse.hudson.service;

import com.google.inject.ImplementedBy;
import hudson.model.Hudson;
import hudson.model.Queue;
import org.acegisecurity.AccessDeniedException;
import org.eclipse.hudson.service.internal.QueueServiceImpl;

/**
 * Default implementation of {@link QueueService}
 *
 * @since 2.1.0
 */
@ImplementedBy(QueueServiceImpl.class)
public interface QueueService
{
    /**
     * Get the queue.
     *
     * @return the master {@link Queue}
     * @throws AccessDeniedException if current context does not have {@link Hudson#ADMINISTER} permission.
     */
    Queue getQueue();
}
