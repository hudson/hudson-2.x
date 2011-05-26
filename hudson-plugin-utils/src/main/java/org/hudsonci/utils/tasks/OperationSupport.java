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

package org.hudsonci.utils.tasks;

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
