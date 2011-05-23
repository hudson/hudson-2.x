/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.dependencymonitor;

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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Subscribe to {@link DependencyMonitor} dependency updates.  Projects that subscribe are artifact consumers.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@XStreamAlias("maven-dependency-trigger")
public class DependencyTrigger
    extends Trigger<AbstractProject>
{
    private static final Logger log = LoggerFactory.getLogger(DependencyTrigger.class);

    @DataBoundConstructor
    public DependencyTrigger() {
        // empty
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public AbstractProject getProject() {
        return job;
    }

    @Override
    public void start(final AbstractProject project, final boolean newInstance) {
        super.start(project, newInstance);
        getDescriptor().dependencyMonitor.subscribe(getProject());
    }

    @Override
    public void stop() {
        getDescriptor().dependencyMonitor.unsubscribe(getProject());
    }

    @Named
    @Singleton
    @Typed(Descriptor.class)
    public static class DescriptorImpl
        extends TriggerDescriptor
    {
        private final DependencyMonitor dependencyMonitor;

        @Inject
        public DescriptorImpl(final DependencyMonitor dependencyMonitor) {
            this.dependencyMonitor = checkNotNull(dependencyMonitor);
        }

        @Override
        public boolean isApplicable(final Item item) {
            return item instanceof Project || item instanceof MatrixProject;
        }

        @Override
        public String getDisplayName() {
            // TODO: i18n
            return "Build when Maven dependencies have been updated";
        }
    }
}
