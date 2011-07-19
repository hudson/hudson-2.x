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

package org.eclipse.hudson.maven.plugin.dependencymonitor;

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
 * @since 2.1.0
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
            return "Build when Maven dependencies have been updated by Maven " +
                    "3 integration";
        }
    }
}
