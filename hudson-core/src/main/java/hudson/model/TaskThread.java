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
*    Kohsuke Kawaguchi, Red Hat, Inc.
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.console.AnnotatedLargeText;
import hudson.util.StreamTaskListener;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import org.kohsuke.stapler.framework.io.LargeText;
import org.kohsuke.stapler.framework.io.ByteBuffer;

/**
 * {@link Thread} for performing one-off task.
 *
 * <p>
 * Designed to be used inside {@link TaskAction}.
 *
 * 
 *
 * @author Kohsuke Kawaguchi
 * @since 1.191
 * @see TaskAction
 */
public abstract class TaskThread extends Thread {
    /**
     * @deprecated as of Hudson 1.350
     *      Use {@link #log}. It's the same object, in a better type.
     */
    private final LargeText text;

    /**
     * Represents the output from this task thread.
     */
    private final AnnotatedLargeText<TaskThread> log;

    /**
     * Represents the interface to produce output.
     */
    private TaskListener listener;

    private final TaskAction owner;

    private volatile boolean isRunning;

    /**
     *
     * @param output
     *      Determines where the output from this task thread goes.
     */
    protected TaskThread(TaskAction owner, ListenerAndText output) {
        //FIXME this failed to compile super(owner.getBuild().toString()+' '+owner.getDisplayName());
        //Please implement more general way how to get information about action owner, 
        //if you want it in the thread's name.
        super(owner.getDisplayName());
        this.owner = owner;
        this.text = this.log = output.text;
        this.listener = output.listener;
    }

    public Reader readAll() throws IOException {
        // this method can be invoked from another thread.
        return text.readAll();
    }

    /**
     * Registers that this {@link TaskThread} is run for the specified
     * {@link TaskAction}. This can be explicitly called from subtypes
     * to associate a single {@link TaskThread} across multiple tag actions.
     */
    protected final void associateWith(TaskAction action) {
        action.workerThread = this;
        action.log = new WeakReference<AnnotatedLargeText>(log);
    }

    /**
     * Starts the task execution asynchronously.
     */
    @Override
    public void start() {
        associateWith(owner);
        super.start();
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Determines where the output of this {@link TaskThread} goes.
     * <p>
     * Subclass can override this to send the output to a file, for example.
     */
    protected ListenerAndText createListener() throws IOException {
        return ListenerAndText.forMemory();
    }

    @Override
    public final void run() {
        isRunning = true;
        try {
            perform(listener);
            listener.getLogger().println("Completed");
            owner.workerThread = null;            
        } catch (InterruptedException e) {
            listener.getLogger().println("Aborted");
        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
        } finally {
            listener = null;
            isRunning =false;
        }
        log.markAsComplete();
    }

    /**
     * Do the actual work.
     *
     * @throws Exception
     *      The exception is recorded and reported as a failure.
     */
    protected abstract void perform(TaskListener listener) throws Exception;

    /**
     * Tuple of {@link TaskListener} and {@link AnnotatedLargeText}, representing
     * the interface for producing output and how to retrieve it later.
     */
    public static final class ListenerAndText {
        final TaskListener listener;
        final AnnotatedLargeText<TaskThread> text;

        public ListenerAndText(TaskListener listener, AnnotatedLargeText<TaskThread> text) {
            this.listener = listener;
            this.text = text;
        }

        /**
         * @deprecated as of Hudson 1.350
         *      Use {@link #forMemory(TaskThread)} and pass in the calling {@link TaskAction}
         */
        public static ListenerAndText forMemory() {
            return forMemory(null);
        }

        /**
         * @deprecated as of Hudson 1.350
         *      Use {@link #forFile(File, TaskThread)} and pass in the calling {@link TaskAction}
         */
        public static ListenerAndText forFile(File f) throws IOException {
            return forFile(f,null);
        }

        /**
         * Creates one that's backed by memory.
         */
        public static ListenerAndText forMemory(TaskThread context) {
            // StringWriter is synchronized
            ByteBuffer log = new ByteBuffer();

            return new ListenerAndText(
                new StreamTaskListener(log),
                new AnnotatedLargeText<TaskThread>(log,Charset.defaultCharset(),false,context)
            );
        }

        /**
         * Creates one that's backed by a file. 
         */
        public static ListenerAndText forFile(File f, TaskThread context) throws IOException {
            return new ListenerAndText(
                new StreamTaskListener(f),
                new AnnotatedLargeText<TaskThread>(f,Charset.defaultCharset(),false,context)
            );
        }
    }
}
