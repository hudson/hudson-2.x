/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.util;

import hudson.EnvVars;
import hudson.util.ProcessTree.ProcessCallable;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

/**
 * Remoting interfaces of {@link ProcessTree}.
 *
 * These classes need to be public due to the way {@link Proxy} works.
 *
 * @author Kohsuke Kawaguchi
 */
public class ProcessTreeRemoting {
    public interface IProcessTree {
        void killAll(Map<String, String> modelEnvVars) throws InterruptedException;
    }

    public interface IOSProcess {
        int getPid();
        IOSProcess getParent();
        void kill() throws InterruptedException;
        void killRecursively() throws InterruptedException;
        List<String> getArguments();
        EnvVars getEnvironmentVariables();
        <T> T act(ProcessCallable<T> callable) throws IOException, InterruptedException;
    }
}
