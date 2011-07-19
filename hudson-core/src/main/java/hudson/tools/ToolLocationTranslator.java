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
*    Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.tools;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.slaves.NodeSpecific;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;

/**
 * This Hudson-wide extension points can participate in determining the actual node-specific path
 * of the {@link ToolInstallation} for the given {@link Node}.
 *
 * <p>
 * This extension point is useful when there's a deterministic rule of where tools are installed.
 * One can program such a logic and contribute a {@link ToolLocationTranslator}.
 * Compared to manually specifying {@link ToolLocationNodeProperty}, duplicated configurations
 * can be avoided this way. 
 *
 * <p>
 * Entry point to the translation process is
 *
 * @author Kohsuke Kawaguchi
 * @since 1.299
 * @see ToolInstallation#translateFor(Node, TaskListener)
 */
public abstract class ToolLocationTranslator implements ExtensionPoint {
    /**
     * Called for each {@link ToolInstallation#translateFor(Node, TaskListener)} invocations
     * (which normally means it's invoked for each {@link NodeSpecific#forNode(Node, TaskListener)})
     * to translate the tool location into the node specific location.
     *
     * <p>
     * If this implementation is capable of determining the location, return the path in the absolute file name.
     * (This method doesn't return {@link File} so that it can handle path names of a different OS.
     *
     * <p>
     * Otherwise return null to let other {@link ToolLocationTranslator}s a chance to do translations
     * on their own. 
     */
    public abstract String getToolHome(Node node, ToolInstallation installation, TaskListener log) throws IOException, InterruptedException;

    /**
     * Returns all the registered {@link ToolLocationTranslator}s.
     */
    public static ExtensionList<ToolLocationTranslator> all() {
        return Hudson.getInstance().getExtensionList(ToolLocationTranslator.class);
    }
}
