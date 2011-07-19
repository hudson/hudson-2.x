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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * {@link ThreadFactory} that creates a thread, which in turn displays a stack trace
 * when it terminates unexpectedly.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.226
 */
public class ExceptionCatchingThreadFactory implements ThreadFactory, Thread.UncaughtExceptionHandler {
    private final ThreadFactory core;

    public ExceptionCatchingThreadFactory() {
        this(Executors.defaultThreadFactory());
    }

    public ExceptionCatchingThreadFactory(ThreadFactory core) {
        this.core = core;
    }

    public Thread newThread(Runnable r) {
        Thread t = core.newThread(r);
        t.setUncaughtExceptionHandler(this);
        return t;
    }

    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.log(Level.WARNING, "Thread "+t.getName()+" terminated unexpectedly",e);
    }

    private static final Logger LOGGER = Logger.getLogger(ExceptionCatchingThreadFactory.class.getName());
}
