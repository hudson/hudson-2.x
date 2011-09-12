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

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStep;
import org.apache.commons.lang3.time.StopWatch;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link hudson.tasks.BuildStep#perform} operation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class PerformOperation<T extends BuildStep>
    extends OperationSupport<T>
{
    protected final Launcher launcher;

    public PerformOperation(final T owner, final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) {
        super(owner, build, listener);
        this.launcher = checkNotNull(launcher);
    }

    public boolean execute() throws InterruptedException, IOException {
        StopWatch watch = new StopWatch();
        watch.start();
        log.debug("Executing");
        try {
            boolean result = doExecute();
            log.debug("Finished in {}", watch);
            return result;
        }
        catch (InterruptedException e) {
            log.debug("Failed after {}", watch);
            onFailure(e);
            throw e;
        }
        catch (IOException e) {
            log.debug("Failed after {}", watch);
            onFailure(e);
            throw e;
        }
        catch (Exception e) {
            log.debug("Failed after {}", watch);
            onFailure(e);
            throw new OperationFailure(e);
        }
    }
}
