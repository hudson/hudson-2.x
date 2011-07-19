/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * {@link ThreadFactory} that creates daemon threads.
 *
 * @author Kohsuke Kawaguchi
 */
public class DaemonThreadFactory implements ThreadFactory {
    private final ThreadFactory core;

    public DaemonThreadFactory() {
        this(Executors.defaultThreadFactory());
    }

    public DaemonThreadFactory(ThreadFactory core) {
        this.core = core;
    }

    public Thread newThread(Runnable r) {
        Thread t = core.newThread(r);
        t.setDaemon(true);
        return t;
    }
}
