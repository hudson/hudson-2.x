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

package org.hudsonci.maven.plugin.artifactrecorder;

import org.hudsonci.maven.model.state.ArtifactDTO;
import org.hudsonci.utils.plugin.ui.JellyAccessible;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import org.hudsonci.maven.plugin.artifactrecorder.internal.PerformArchiving;
import org.hudsonci.maven.plugin.builder.BuildStateRecord;
import org.hudsonci.maven.plugin.builder.MavenBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Archives artifacts collected from a {@link MavenBuilder}.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@XStreamAlias("maven-artifact-archiver")
public class ArtifactArchiver 
    extends Recorder
{
    private final boolean includePomArtifacts;
    private final boolean deleteOldArchiveArtifacts;

    @DataBoundConstructor
    public ArtifactArchiver(final boolean includePomArtifacts, final boolean deleteOldArchiveArtifacts) {
        this.includePomArtifacts = includePomArtifacts;
        this.deleteOldArchiveArtifacts = deleteOldArchiveArtifacts;
    }

    @JellyAccessible
    public boolean isIncludePomArtifacts() {
        return includePomArtifacts;
    }

    @JellyAccessible
    public boolean isDeleteOldArchiveArtifacts() {
        return deleteOldArchiveArtifacts;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }
    
    @Override
    public boolean perform(final AbstractBuild build, final Launcher launcher, final BuildListener listener)
        throws InterruptedException, IOException {
        
        if(Result.ABORTED == build.getResult()) {
            return true;
        }
        
        // TODO: consider using guavas iterator mashing/chaining if they can be serialized for the DigestCollector to use
        // and discard duplicates.
        Set<ArtifactDTO> artifacts = new HashSet<ArtifactDTO>();
        for (BuildStateRecord record : build.getActions(BuildStateRecord.class)) {
            artifacts.addAll(record.getState().getArtifacts());
        }
        
        return new PerformArchiving(this, build, launcher, listener, artifacts, includePomArtifacts, deleteOldArchiveArtifacts).execute();
    }

    @Named
    @Singleton
    @Typed(Descriptor.class)
    public static class DescriptorImpl
        extends BuildStepDescriptor<Publisher>
    {
        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Archive Maven 3 artifacts";
        }
    }
}
