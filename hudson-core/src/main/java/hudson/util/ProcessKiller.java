/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.util;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Hudson;
import hudson.util.ProcessTree.OSProcess;

import java.io.IOException;
import java.io.Serializable;

/**
 * Extension point that defines more elaborate way of killing processes, such as
 * sudo or pfexec, for {@link ProcessTree}.
 *
 * <h2>Lifecycle</h2>
 * <p>
 * Each implementation of {@link ProcessKiller} is instantiated once on the master.
 * Whenever a process needs to be killed, those implementations are serialized and sent over
 * to the appropriate slave, then the {@link #kill(OSProcess)} method is invoked
 * to attempt to kill the process.
 *
 * <p>
 * One of the consequences of this design is that the implementation should be stateless
 * and concurrent-safe. That is, the {@link #kill(OSProcess)} method can be invoked by multiple threads
 * concurrently on the single instance.
 *
 * <p>
 * Another consequence of this design is that if your {@link ProcessKiller} requires configuration,
 * it needs to be serializable, and configuration needs to be updated atomically, as another
 * thread may be calling into {@link #kill(OSProcess)} just when you are updating your configuration.
 *
 * @author jpederzolli
 * @author Kohsuke Kawaguchi
 * @since 1.362
 */
public abstract class ProcessKiller implements ExtensionPoint, Serializable {
    /**
     * Returns all the registered {@link ProcessKiller} descriptors.
     */
    public static ExtensionList<ProcessKiller> all() {
        return Hudson.getInstance().getExtensionList(ProcessKiller.class);
    }

    /**
     * Attempts to kill the given process.
     *
     * @param process process to be killed. Always a {@linkplain ProcessTree.Local local process}.
     * @return
     *      true if the killing was successful, and Hudson won't try to use other {@link ProcessKiller}
     *      implementations to kill the process. false if the killing failed or is unattempted, and Hudson will continue
     *      to use the rest of the {@link ProcessKiller} implementations to try to kill the process.
     * @throws IOException
     *      The caller will log this exception and otherwise treat as if the method returned false, and moves on
     *      to the next killer.
     * @throws InterruptedException
     *      if the callee performs a time consuming operation and if the thread is canceled, do not catch
     *      {@link InterruptedException} and just let it thrown from the method.
     */
    public abstract boolean kill(ProcessTree.OSProcess process) throws IOException, InterruptedException;

    private static final long serialVersionUID = 1L;
}
