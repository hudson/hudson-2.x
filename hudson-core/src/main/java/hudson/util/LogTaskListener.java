/*******************************************************************************
 *
 * Copyright (c) 2009, Oracle Corporation
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

import hudson.console.ConsoleNote;
import hudson.model.TaskListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * {@link TaskListener} which sends messages to a {@link Logger}.
 */
public class LogTaskListener extends AbstractTaskListener implements Serializable {
    
    private final TaskListener delegate;

    public LogTaskListener(Logger logger, Level level) {
        delegate = new StreamTaskListener(new LogOutputStream(logger, level, new Throwable().getStackTrace()[1]));
    }

    public PrintStream getLogger() {
        return delegate.getLogger();
    }

    public PrintWriter error(String msg) {
        return delegate.error(msg);
    }

    public PrintWriter error(String format, Object... args) {
        return delegate.error(format, args);
    }

    public PrintWriter fatalError(String msg) {
        return delegate.fatalError(msg);
    }

    public PrintWriter fatalError(String format, Object... args) {
        return delegate.fatalError(format, args);
    }

    public void annotate(ConsoleNote ann) {
        // no annotation support
    }

    public void close() {
        delegate.getLogger().close();
    }

    private static class LogOutputStream extends OutputStream {

        private final Logger logger;
        private final Level level;
        private final StackTraceElement caller;
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        public LogOutputStream(Logger logger, Level level, StackTraceElement caller) {
            this.logger = logger;
            this.level = level;
            this.caller = caller;
        }

        public void write(int b) throws IOException {
            if (b == '\r' || b == '\n') {
                flush();
            } else {
                baos.write(b);
            }
        }

        @Override
        public void flush() throws IOException {
            if (baos.size() > 0) {
                LogRecord lr = new LogRecord(level, baos.toString());
                lr.setLoggerName(logger.getName());
                lr.setSourceClassName(caller.getClassName());
                lr.setSourceMethodName(caller.getMethodName());
                logger.log(lr);
            }
            baos.reset();
        }

        @Override
        public void close() throws IOException {
            flush();
        }

    }

    private static final long serialVersionUID = 1L;
}
