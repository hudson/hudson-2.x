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
*    Kohsuke Kawaguchi, Thomas J. Black
 *     
 *
 *******************************************************************************/ 

package hudson.node_monitors;

import hudson.ExtensionPoint;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.tasks.Publisher;
import hudson.model.Computer;
import hudson.model.ComputerSet;
import hudson.model.Describable;
import hudson.model.Node;
import hudson.model.Hudson;
import hudson.model.Descriptor;
import hudson.util.DescriptorList;

import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Extension point for managing and monitoring {@link Node}s.
 *
 * <h2>Views</h2>
 * <dl>
 * <dt>column.jelly</dt>
 * <dd>
 * Invoked from {@link ComputerSet} <tt>index.jelly</tt> to render a column.
 * The {@link NodeMonitor} instance is accessible through the "from" variable.
 * Also see {@link #getColumnCaption()}.
 *
 * <dt>config.jelly (optional)</dt>
 * <dd>
 * Configuration fragment to be displayed in {@code http://server/hudson/computer/configure}.
 * Used for configuring the threshold for taking nodes offline. 
 * </dl>
 *
 * <h2>Persistence</h2>
 * <p>
 * {@link NodeMonitor}s are persisted via XStream.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.123
 */
@ExportedBean
public abstract class NodeMonitor implements ExtensionPoint, Describable<NodeMonitor> {
    private volatile boolean ignored;

    /**
     * Returns the name of the column to be added to {@link ComputerSet} index.jelly.
     *
     * @return
     *      null to not render a column. The convention is to use capitalization like "Foo Bar Zot".
     */
    @Exported
    public String getColumnCaption() {
        return getDescriptor().getDisplayName();
    }

    public AbstractNodeMonitorDescriptor<?> getDescriptor() {
        return (AbstractNodeMonitorDescriptor<?>)Hudson.getInstance().getDescriptorOrDie(getClass());
    }

    public Object data(Computer c) {
        return getDescriptor().get(c);
    }

    /**
     * Starts updating the data asynchronously.
     * If there's any previous updating activity going on, it'll be interrupted and aborted.
     *
     * @return
     *      {@link Thread} object that carries out the update operation.
     *      You can use this to interrupt the execution or waits for the completion.
     *      Always non-null
     * @since 1.232
     */
    public Thread triggerUpdate() {
        return getDescriptor().triggerUpdate();
    }

    /**
     * Obtains all the instances of {@link NodeMonitor}s that are alive.
     * @since 1.187
     */
    public static List<NodeMonitor> getAll() {
        return ComputerSet.getMonitors().toList();
    }

    /**
     * True if this monitoring shouldn't mark the slaves offline.
     *
     * <p>
     * Many {@link NodeMonitor}s implement a logic that if the value goes above/below
     * a threshold, the slave will be marked offline as a preventive measure.
     * This flag controls that.
     *
     * <p>
     * Unlike {@link Publisher}, where the absence of an instance indicates that it's disengaged,
     * in {@link NodeMonitor} this boolean flag is used to indicate the disengagement, so that
     * monitors work in opt-out basis.
     */
    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    /**
     * All registered {@link NodeMonitor}s.
     * @deprecated as of 1.286.
     *      Use {@link #all()} for read access and {@link Extension} for registration.
     */
    public static final DescriptorList<NodeMonitor> LIST = new DescriptorList<NodeMonitor>(NodeMonitor.class);

    /**
     * Returns all the registered {@link NodeMonitor} descriptors.
     */
    public static DescriptorExtensionList<NodeMonitor,Descriptor<NodeMonitor>> all() {
        return Hudson.getInstance().<NodeMonitor,Descriptor<NodeMonitor>>getDescriptorList(NodeMonitor.class);
    }
}
