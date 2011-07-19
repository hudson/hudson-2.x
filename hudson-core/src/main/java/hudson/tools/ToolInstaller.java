/*******************************************************************************
 *
 * Copyright (c) 2009-2010, Oracle Corporation
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

package hudson.tools;

import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Hudson;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.TaskListener;
import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * An object which can ensure that a generic {@link ToolInstallation} in fact exists on a node.
 *
 * The subclass should have a {@link ToolInstallerDescriptor}.
 * A {@code config.jelly} should be provided to customize specific fields;
 * {@code <t:label xmlns:t="/hudson/tools"/>} to customize {@code label}.
 * @see <a href="http://wiki.hudson-ci.org/display/HUDSON/Tool+Auto-Installation">Tool Auto-Installation</a>
 * @since 1.305
 */
public abstract class ToolInstaller implements Describable<ToolInstaller>, ExtensionPoint {

    private final String label;

    protected transient ToolInstallation tool;

    /**
     * Subclasses should pass these parameters in using {@link DataBoundConstructor}.
     */
    protected ToolInstaller(String label) {
        this.label = Util.fixEmptyAndTrim(label);
    }

    /**
     * Called during the initialization to tell {@link ToolInstaller} what {@link ToolInstallation}
     * it is configured against.
     */
    protected void setTool(ToolInstallation t) {
        this.tool = t;
    }

    /**
     * Label to limit which nodes this installation can be performed on.
     * Can be null to not impose a limit.
     */
    public final String getLabel() {
        return label;
    }

    /**
     * Checks whether this installer can be applied to a given node.
     * (By default, just checks the label.)
     */
    public boolean appliesTo(Node node) {
        Label l = Hudson.getInstance().getLabel(label);
        return l == null || l.contains(node);
    }

    /**
     * Ensure that the configured tool is really installed.
     * If it is already installed, do nothing.
     * Called only if {@link #appliesTo(Node)} are true.
     * @param tool the tool being installed
     * @param node the computer on which to install the tool
     * @param log any status messages produced by the installation go here
     * @return the (directory) path at which the tool can be found,
     *         typically coming from {@link #preferredLocation}
     * @throws IOException if installation fails
     * @throws InterruptedException if communication with a slave is interrupted
     */
    public abstract FilePath performInstallation(ToolInstallation tool, Node node, TaskListener log) throws IOException, InterruptedException;

    /**
     * Convenience method to find a location to install a tool.
     * @param tool the tool being installed
     * @param node the computer on which to install the tool
     * @return {@link ToolInstallation#getHome} if specified, else a path within the local
     *         Hudson work area named according to {@link ToolInstallation#getName}
     * @since 1.310
     */
    protected final FilePath preferredLocation(ToolInstallation tool, Node node) {
        if (node == null) {
            throw new IllegalArgumentException("must pass non-null node");
        }
        String home = Util.fixEmptyAndTrim(tool.getHome());
        if (home == null) {
            // XXX should this somehow uniquify paths among ToolInstallation.all()?
            home = tool.getName().replaceAll("[^A-Za-z0-9_.-]+", "_");
        }
        FilePath root = node.getRootPath();
        if (root == null) {
            throw new IllegalArgumentException("Node " + node.getDisplayName() + " seems to be offline");
        }
        return root.child("tools").child(home);
    }

    public ToolInstallerDescriptor<?> getDescriptor() {
        return (ToolInstallerDescriptor) Hudson.getInstance().getDescriptorOrDie(getClass());
    }
}
