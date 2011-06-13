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

package org.hudsonci.maven.plugin.install;

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
