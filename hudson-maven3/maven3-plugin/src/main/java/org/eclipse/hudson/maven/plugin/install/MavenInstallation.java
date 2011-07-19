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

package org.eclipse.hudson.maven.plugin.install;

import org.eclipse.hudson.utils.plugin.ui.StaplerAccessible;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import hudson.EnvVars;
import hudson.XmlFile;
import hudson.model.EnvironmentSpecific;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Maven installation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@XStreamAlias("maven-installation")
@XStreamInclude({
    MavenInstallation.DescriptorImpl.class,
    UrlMavenInstaller.class
})
public class MavenInstallation
    extends ToolInstallation
    implements EnvironmentSpecific<MavenInstallation>, NodeSpecific<MavenInstallation>
{
    private static final Logger log = LoggerFactory.getLogger(MavenInstallation.class);

    public static final String DESCRIPTOR_FILE_NAME = "maven-installation-descriptor.xml";

    // FIXME: ToolInstallation really needs a UUID to be used for reference, so that if the tool is renamed it doesn't instantly break all builders

    @DataBoundConstructor
    public MavenInstallation(final String name, final String home, final List<? extends ToolProperty<?>> properties) {
        super(name, home, properties);
    }

    private MavenInstallation(final MavenInstallation source, final String home, final List<? extends ToolProperty<?>> properties) {
        this(source.getName(), home, properties);
    }

    public MavenInstallation forEnvironment(final EnvVars env) {
        return new MavenInstallation(this, env.expand(getHome()), getProperties().toList());
    }

    public MavenInstallation forNode(final Node node, final TaskListener log) throws IOException, InterruptedException {
        return new MavenInstallation(this, translateFor(node, log), getProperties().toList());
    }

    @Named
    @Singleton
    @Typed(hudson.model.Descriptor.class)
    @XStreamAlias("maven-installation-descriptor")
    public static class DescriptorImpl
        extends ToolDescriptor<MavenInstallation>
    {
        private volatile MavenInstallation[] installations;

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Maven 3";
        }

        @Override
        public MavenInstallation[] getInstallations() {
            if (installations == null) {
                return new MavenInstallation[0];
            }
            return installations;
        }

        @Override
        public void setInstallations(final MavenInstallation... installations) {
            assert installations != null;
            this.installations = installations;
            save();
        }

        // TODO: Add default installer, once we have a more comprehensive network/url/nexus-based installer

        @Override
        public XmlFile getConfigFile() {
            return new XmlFile(new File(Hudson.getInstance().getRootDir(), DESCRIPTOR_FILE_NAME));
        }

        @StaplerAccessible
        public FormValidation doCheckName(@QueryParameter(fixEmpty=true, required=true) String value) {
            log.trace("Validate name: {}", value);

            if (value == null) {
                return FormValidation.error("Required");
            }

            if (value.trim().equals(BundledMavenInstallation.NAME)) {
                return FormValidation.error("Illegal use of reserved name");
            }

            return FormValidation.ok();
        }

        // Not checking home, as when "Install Automatically" is flipped on then we just end up with a constant error. :-(
    }
}
