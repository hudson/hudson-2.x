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

package org.eclipse.hudson.utils.tasks;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.gossip.support.MuxLoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link hudson.tasks.BuildStep} operations.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class OperationSupport<T extends BuildStep>
{
    /**
     * Server-side logger.
     */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final T owner;

    protected final AbstractBuild<?,?> build;

    protected final BuildListener listener;

    /**
     * Build console logger.
     */
    protected final TaskListenerLogger logger;

    /**
     * Muxed server+build console logger.
     */
    protected final Logger muxlog;

    public OperationSupport(final T owner, final AbstractBuild<?,?> build, final BuildListener listener) {
        this.owner = checkNotNull(owner);
        this.build = checkNotNull(build);
        this.listener = checkNotNull(listener);
        this.logger = new TaskListenerLogger(listener);
        this.muxlog = MuxLoggerFactory.create(log, logger);
    }

    protected abstract boolean doExecute() throws Exception;

    protected void onFailure(final Throwable cause) {
        logger.error("Failure: {}", cause.toString());
        log.trace("Failure details", cause);
    }
}
