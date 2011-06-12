/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.plugins.snapshotmonitor;

import org.hudsonci.utils.plugin.ui.JellyAccessible;
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
