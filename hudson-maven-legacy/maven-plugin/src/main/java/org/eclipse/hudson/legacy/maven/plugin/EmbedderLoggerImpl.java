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
*    Kohsuke Kawaguchi, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin;

import hudson.model.TaskListener;

import java.io.PrintStream;
import java.util.StringTokenizer;

import org.apache.maven.cli.MavenLoggerManager;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

/**
 * {@link MavenEmbedderLogger} implementation that
 * sends output to {@link TaskListener}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class EmbedderLoggerImpl extends MavenLoggerManager {
    private final PrintStream logger;

    public EmbedderLoggerImpl(TaskListener listener, int threshold) {
        super(new ConsoleLogger( threshold, "hudson-logger" ));
        logger = listener.getLogger();
    }

    private void print(String message, Throwable throwable, int threshold, String prefix) {
        if (getThreshold() <= threshold) {
            StringTokenizer tokens = new StringTokenizer(message,"\n");
            while(tokens.hasMoreTokens()) {
                logger.print(prefix);
                logger.println(tokens.nextToken());
            }

            if (throwable!=null)
                throwable.printStackTrace(logger);
        }
    }

    public void debug(String message, Throwable throwable) {
        print(message, throwable, Logger.LEVEL_DEBUG, "[DEBUG] ");
    }

    public void info(String message, Throwable throwable) {
        print(message, throwable, Logger.LEVEL_INFO, "[INFO ] ");
    }

    public void warn(String message, Throwable throwable) {
        print(message, throwable, Logger.LEVEL_WARN, "[WARN ] ");
    }

    public void error(String message, Throwable throwable) {
        print(message, throwable, Logger.LEVEL_ERROR, "[ERROR] ");
    }

    public void fatalError(String message, Throwable throwable) {
        print(message, throwable, Logger.LEVEL_FATAL, "[FATAL] ");
    }
}
