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

package org.eclipse.hudson.plugins.snapshotmonitor;

import org.eclipse.hudson.utils.plugin.ui.JellyAccessible;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.Project;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Trigger builds for a job whose SNAPSHOT dependencies have changed externally.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@XStreamAlias("maven-snapshot-trigger")
public class SnapshotTrigger
    extends Trigger<AbstractProject>
{
    private static final Logger log = LoggerFactory.getLogger(SnapshotTrigger.class);

    private final boolean excludeInternallyProduced;

    @DataBoundConstructor
    public SnapshotTrigger(final String spec, final boolean excludeInternallyProduced) throws Exception {
        super(spec);
        this.excludeInternallyProduced = excludeInternallyProduced;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @JellyAccessible
    public boolean isExcludeInternallyProduced() {
        return excludeInternallyProduced;
    }

    @Override
    public void start(final AbstractProject project, final boolean newInstance) {
        super.start(project, newInstance);

        // Early warning that the monitor is not properly configured
        if (!getDescriptor().snapshotMonitor.isConfigured()) {
            log.warn("SNAPSHOT monitor has not been configured");
        }
    }

    @Override
    public void run() {
        try {
            getDescriptor().snapshotMonitor.check(job);
        }
        catch (IOException e) {
            log.error("Failure occurred while checking", e);
        }
    }

    @Named
    @Singleton
    @Typed(Descriptor.class)
    public static class DescriptorImpl
        extends TriggerDescriptor
    {
        private final SnapshotMonitor snapshotMonitor;

        @Inject
        public DescriptorImpl(final SnapshotMonitor snapshotMonitor) {
            this.snapshotMonitor = checkNotNull(snapshotMonitor);
        }

        @Override
        public String getDisplayName() {
            // TODO: Use localizer
            return "Build when Maven SNAPSHOT dependencies have been updated externally";
        }

        @Override
        public boolean isApplicable(final Item item) {
            return item instanceof MatrixProject || item instanceof Project;
        }
    }
}
