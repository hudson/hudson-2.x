/*******************************************************************************
 *
 * Copyright (c) 2010 Yahoo! Inc.
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

package hudson.console;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A hook to allow filtering of information that is written to the console log.
 * Unlike {@link ConsoleAnnotator} and {@link ConsoleNote}, this class provides
 * direct access to the underlying {@link OutputStream} so it's possible to suppress
 * data, which isn't possible from the other interfaces.
 * 
 * @author dty
 * @since 1.383
 */
public abstract class ConsoleLogFilter implements ExtensionPoint {
    /**
     * Called on the start of each build, giving extensions a chance to intercept
     * the data that is written to the log.
     */
    public abstract OutputStream decorateLogger(AbstractBuild build, OutputStream logger) throws IOException, InterruptedException;

    /**
     * All the registered {@link ConsoleLogFilter}s.
     */
    public static ExtensionList<ConsoleLogFilter> all() {
        return Hudson.getInstance().getExtensionList(ConsoleLogFilter.class);
    }
}
