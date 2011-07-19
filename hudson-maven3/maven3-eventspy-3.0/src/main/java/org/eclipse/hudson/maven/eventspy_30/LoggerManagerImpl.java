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

package org.eclipse.hudson.maven.eventspy_30;

import org.apache.maven.eventspy.EventSpy;
import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.model.hudson.maven.eventspy.common.Callback;

import static com.google.common.base.Preconditions.checkArgument;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.codehaus.plexus.logging.Logger.LEVEL_DEBUG;
import static org.codehaus.plexus.logging.Logger.LEVEL_DISABLED;
import static org.codehaus.plexus.logging.Logger.LEVEL_ERROR;
import static org.codehaus.plexus.logging.Logger.LEVEL_FATAL;
import static org.codehaus.plexus.logging.Logger.LEVEL_INFO;
import static org.codehaus.plexus.logging.Logger.LEVEL_WARN;

/**
 * Bridges logging over {@link Callback}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class LoggerManagerImpl
    implements LoggerManager
{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerManagerImpl.class);

    private final EventSpy spy;

    private final LoggerManager delegate;

    private int threshold = LEVEL_DEBUG;

    public LoggerManagerImpl(final EventSpy spy, final LoggerManager delegate) {
        this.spy = checkNotNull(spy);
        this.delegate = checkNotNull(delegate);
    }

    //
    // TODO: Figure out if we need to invoke more delegate methods... to get the default logging working.
    //

    public void setThreshold(final int threshold) {
        this.threshold = threshold;
    }

    /**
     * Same as {@link #setThreshold}, we do not track logger adapters.
     */
    public void setThresholds(final int threshold) {
        setThreshold(threshold);
    }

    public int getThreshold() {
        return threshold;
    }

    public int getThreshold(final String role) {
        return getThreshold();
    }

    /**
     * Not supported.
     */
    public int getThreshold(final String role, final String roleHint) {
        return getThreshold();
    }

    public Logger getLoggerForComponent(final String role) {
        return getLoggerForComponent(role, null);
    }

    public Logger getLoggerForComponent(final String role, final String roleHint) {
        checkNotNull(role);

        Logger logger = new LoggerImpl(getThreshold(), toLoggerName(role, roleHint),
            delegate.getLoggerForComponent(role, roleHint));

        log.debug("Created logger: {}", logger);

        return logger;
    }

    /**
     * Not supported.
     */
    public void returnComponentLogger(final String role, final String hint) {
        if (log.isDebugEnabled()) {
            log.debug("Ignoring logger return; name={}", toLoggerName(role,hint));
        }
    }

    public void returnComponentLogger(final String role) {
        returnComponentLogger(role, null);
    }

    /**
     * Not supported.
     */
    public int getActiveLoggerCount() {
        return -1;
    }

    private String toLoggerName(final String role, final String roleHint) {
        if (roleHint == null) {
            return role;
        }
        else {
            return String.format("%s#%s", role, roleHint);
        }
    }

    /**
     * Container for log event details.
     */
    public static class LogEvent
    {
        private final Logger logger;

        private final int level;

        private final String message;

        private final Throwable cause;

        public LogEvent(final Logger logger, final int level, final String message, final Throwable cause) {
            this.logger = checkNotNull(logger);
            checkArgument(level >= LEVEL_DEBUG && level <= LEVEL_DISABLED);
            this.level = level;
            this.message = checkNotNull(message);
            // cause might be null
            this.cause = cause;
        }

        public Logger getLogger() {
            return logger;
        }

        public int getLevel() {
            return level;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getCause() {
            return cause;
        }

        public void delegate() {
            switch (level) {
                case LEVEL_DEBUG:
                    logger.debug(message, cause);
                    break;
                case LEVEL_INFO:
                    logger.info(message, cause);
                    break;
                case LEVEL_WARN:
                    logger.warn(message, cause);
                    break;
                case LEVEL_ERROR:
                    logger.error(message, cause);
                    break;
                case LEVEL_FATAL:
                    logger.fatalError(message, cause);
                    break;
                default:
                    throw new Error();
            }
        }

        @Override
        public String toString() {
            return "LogEvent{" +
                    logger.getName() +
                    ": level=" + level +
                    ", message='" + message + '\'' +
                    ", cause=" + cause +
                    '}';
        }
    }

    /**
     * Logger which turns method calls into {@link LogEvent} instances for processing.
     */
    private class LoggerImpl
        extends AbstractLogger
    {
        private Logger delegate;

        public LoggerImpl(final int threshold, final String name, final Logger delegate) {
            super(threshold, name);
            this.delegate = checkNotNull(delegate);
        }

        private void emitLog(final int level, final String message, final Throwable cause) {
            if (threshold <= level) {
                LogEvent event = new LogEvent(delegate, level, message, cause);

                // FIXME: Figure out how to pipe to handler.
                // FIXME: Log events can come in before we are fully opened, as well as after we are closed
                // FIXME: May also avoid threshold check here and always pass on and only delegate if in range?
                // FIXME: If we are going to leave the output as-is then probably always want to delegate here?

                log.debug("{}", event);
            }
        }

        public void debug(final String message, final Throwable cause) {
            emitLog(LEVEL_DEBUG, message, cause);
        }

        public void info(final String message, final Throwable cause) {
            emitLog(LEVEL_INFO, message, cause);
        }

        public void warn(final String message, final Throwable cause) {
            emitLog(LEVEL_WARN, message, cause);
        }

        public void error(final String message, final Throwable cause) {
            emitLog(LEVEL_ERROR, message, cause);
        }

        public void fatalError(final String message, final Throwable cause) {
            emitLog(LEVEL_FATAL, message, cause);
        }

        public Logger getChildLogger(final String name) {
            return new LoggerImpl(getThreshold(), String.format("%s.%s", getName(), name), delegate.getChildLogger(name));
        }

        @Override
        public String toString() {
            return "LoggerImpl{" +
                "name='" + getName() + '\'' +
                ", threshold=" + threshold +
                '}';
        }
    }
}
