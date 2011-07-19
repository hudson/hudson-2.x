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

package org.eclipse.hudson.legacy.maven.plugin;

import hudson.console.ConsoleNote;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.Result;
import hudson.model.StreamBuildListener;
import hudson.util.AbstractTaskListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;

/**
 * Delegating {@link BuildListener} that can have "side" {@link OutputStream}
 * that gets log outputs. The side stream can be changed at runtime.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.133
 */
final class SplittableBuildListener extends AbstractTaskListener implements BuildListener, Serializable {
    /**
     * The actual {@link BuildListener} where the output goes.
     */
    private final BuildListener core;

    /**
     * Used to accumulate data when no one is claiming the {@link #side},
     * so that the next one who set the {@link #side} can claim all the data.
     */
    private ByteArrayOutputStream unclaimed = new ByteArrayOutputStream();

    private OutputStream side = unclaimed;

    /**
     * Constant {@link PrintStream} connected to both {@link #core} and {@link #side}.
     * This is so that we can change the side stream without the client noticing it.
     */
    private final PrintStream logger;

    public SplittableBuildListener(BuildListener core) {
        this.core = core;
        final OutputStream base = core.getLogger();
        logger = new PrintStream(new OutputStream() {
            public void write(int b) throws IOException {
                base.write(b);
                side.write(b);
            }

            public void write(byte b[], int off, int len) throws IOException {
                base.write(b,off,len);
                side.write(b,off,len);
            }

            public void flush() throws IOException {
                base.flush();
                side.flush();
            }

            public void close() throws IOException {
                base.close();
                side.close();
            }
        });
    }
    
    public void setSideOutputStream(OutputStream os) throws IOException {
        if(os==null) {
            os = unclaimed;
        } else {
            os.write(unclaimed.toByteArray());
            unclaimed = new ByteArrayOutputStream();
        }
        this.side = os;
    }

    public void started(List<Cause> causes) {
        core.started(causes);
    }

    public void finished(Result result) {
        core.finished(result);
    }

    public PrintStream getLogger() {
        return logger;
    }

    public PrintWriter error(String msg) {
        core.error(msg);
        return new PrintWriter(logger);
    }

    public PrintWriter error(String format, Object... args) {
        core.error(format,args);
        return new PrintWriter(logger);
    }

    public PrintWriter fatalError(String msg) {
        core.fatalError(msg);
        return new PrintWriter(logger);
    }

    public PrintWriter fatalError(String format, Object... args) {
        core.fatalError(format,args);
        return new PrintWriter(logger);
    }

    public void annotate(ConsoleNote ann) throws IOException {
        core.annotate(ann);
    }

    private Object writeReplace() throws IOException {
        return new StreamBuildListener(logger);
    }

    private static final long serialVersionUID = 1L;
}
