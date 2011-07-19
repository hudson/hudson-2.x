/*******************************************************************************
 *
 * Copyright (c) 2004-2011, Oracle Corporation
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

package hudson.lifecycle;

import hudson.model.Hudson;
import hudson.util.jna.NativeAccessException;
import hudson.util.jna.NativeFunction;
import hudson.util.jna.NativeUtils;
import java.io.IOException;

/**
 * {@link Lifecycle} implementation when Hudson runs on the embedded
 * servlet container on Unix.
 *
 * <p>
 * Restart by exec to self.
 *
 * @author Kohsuke Kawaguchi, Winston Prakash
 * @since 1.304
 */
public class UnixLifecycle extends Lifecycle {

    private Throwable failedToObtainArgs;

    @Override
    public void restart() throws IOException, InterruptedException {
        Hudson h = Hudson.getInstance();
        if (h != null) {
            h.cleanUp();
        }
        try {
            NativeUtils.getInstance().restartJavaProcess(null, false);
        } catch (NativeAccessException exc) {
            //TODO: Rethrow as IOException to avoid adding NativeExecutionException in throws clause
            throw new IOException(exc);
        }
    }

    @Override
    public void verifyRestartable() throws RestartNotSupportedException {
        // see http://lists.apple.com/archives/cocoa-dev/2005/Oct/msg00836.html and
        // http://factor-language.blogspot.com/2007/07/execve-returning-enotsup-on-mac-os-x.html
        // on Mac, execv fails with ENOTSUP if the caller is multi-threaded, resulting in an error like
        // the one described in http://www.nabble.com/Restarting-hudson-not-working-on-MacOS--to24641779.html
        if (Hudson.isDarwin()) {
            throw new RestartNotSupportedException("Restart is not supported on Mac OS X");
        } else {
            try {

                if (!NativeUtils.getInstance().canRestartJavaProcess()) {
                    throw new RestartNotSupportedException("Restart is not supported on this Platform");
                }
            } catch (NativeAccessException exc) {
                throw new RestartNotSupportedException("Restart is not supported on this Platform");
            }
        }
    }
}
