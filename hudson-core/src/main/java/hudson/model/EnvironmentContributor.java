/*******************************************************************************
 *
 * Copyright (c) 2010, CloudBees, Inc.
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

package hudson.model;

import hudson.EnvVars;
import hudson.ExtensionList;
import hudson.ExtensionPoint;

import java.io.IOException;

/**
 * Contributes environment variables to builds.
 *
 * <p>
 * This extension point can be used to externally add environment variables. Aside from adding environment variables
 * of the fixed name, a typical strategy is to look for specific {@link JobProperty}s and other similar configurations
 * of {@link Job}s to compute values.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.392
 */
public abstract class EnvironmentContributor implements ExtensionPoint {
    /**
     * Contributes environment variables used for a build.
     *
     * <p>
     * This method can be called repeatedly for the same {@link Run}, thus
     * the computation of this method needs to be efficient. If you have a time-consuming
     * computation, one strategy is to take the hit once and then add the result as {@link InvisibleAction}
     * to {@link Run}, then reuse those values later on.
     *
     * <p>
     * This method gets invoked concurrently for multiple {@link Run}s that are being built at the same time,
     * so it must be concurrent-safe.
     *
     * @param r
     *      Build that's being performed. Never null.
     * @param envs
     *      Partially built environment variable map. Implementation of this method is expected to
     *      add additional variables here. Never null.
     * @param listener
     *      Connected to the build console. Can be used to report errors. Never null.
     */
    public abstract void buildEnvironmentFor(Run r, EnvVars envs, TaskListener listener) throws IOException, InterruptedException;

    /**
     * Returns all the registered {@link EnvironmentContributor}s.
     */
    public static ExtensionList<EnvironmentContributor> all() {
        return Hudson.getInstance().getExtensionList(EnvironmentContributor.class);
    }
}
