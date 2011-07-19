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

import hudson.FilePath;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolInstallerDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Installs Maven from a URL.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class UrlMavenInstaller
    extends ToolInstaller
{
    private static final Logger log = LoggerFactory.getLogger(UrlMavenInstaller.class);

    // FIXME: There is already ZipExtractionInstaller which does this sorta
    // FIXME: ... but doesn't like auth to RSO in URL, needs explicit name of dir in archive.

    @DataBoundConstructor
    public UrlMavenInstaller(final String id) {
        super(id);
    }

    @Override
    public FilePath performInstallation(final ToolInstallation tool, final Node node, final TaskListener log)
        throws IOException, InterruptedException
    {
        return null;
    }

//    @Named
//    @Singleton
//    @Typed(hudson.model.Descriptor.class)
    public static final class DescriptorImpl
        extends ToolInstallerDescriptor<UrlMavenInstaller>
    {
        public String getDisplayName() {
            return "Install from URL";
        }

        @Override
        public boolean isApplicable(final Class<? extends ToolInstallation> type) {
            return type == MavenInstallation.class;
        }
    }
}
