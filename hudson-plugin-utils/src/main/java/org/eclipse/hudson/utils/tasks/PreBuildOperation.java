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
import org.apache.commons.lang3.time.StopWatch;

/**
 * Support for {@link hudson.tasks.BuildStep#prebuild} operation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public abstract class PreBuildOperation<T extends BuildStep>
    extends OperationSupport<T>
{
    public PreBuildOperation(final T owner, final AbstractBuild<?,?> build, final BuildListener listener) {
        super(owner, build, listener);
    }

    public boolean execute() {
        StopWatch watch = new StopWatch();
        watch.start();

        log.debug("Executing");
        try {
            boolean result = doExecute();
            log.debug("Finished in {}", watch);
            return result;
        }
        catch (Exception e) {
            log.debug("Failed after {}", watch);
            onFailure(e);
            throw new OperationFailure(e);
        }
    }
}
