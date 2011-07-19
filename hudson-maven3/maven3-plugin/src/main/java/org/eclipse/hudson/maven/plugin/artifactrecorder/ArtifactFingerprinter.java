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

package org.eclipse.hudson.maven.plugin.artifactrecorder;

import org.eclipse.hudson.maven.plugin.artifactrecorder.internal.PerformFingerprinting;
import org.eclipse.hudson.maven.plugin.builder.BuildStateRecord;
import org.eclipse.hudson.maven.plugin.builder.MavenBuilder;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.FingerprintMap;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fingerprints artifacts collected from a {@link MavenBuilder}.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@XStreamAlias("maven-artifact-fingerprinter")
public class ArtifactFingerprinter 
    extends Recorder
{
    @XStreamOmitField
    private FingerprintMap registry;

    @DataBoundConstructor
    public ArtifactFingerprinter() {
        // For Stapler.
    }
    
    @Inject
    public void setFingerprintRegistry(final FingerprintMap fingerprintRegistry) {
        this.registry = fingerprintRegistry;
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
        List<BuildStateRecord> records = build.getActions(BuildStateRecord.class);
        Set<ArtifactDTO> artifacts = new HashSet<ArtifactDTO>();
        for (BuildStateRecord record : records) {
            artifacts.addAll(record.getState().getArtifacts());
        }

        return new PerformFingerprinting(this, build, launcher, listener, records.size(), artifacts, registry).execute();
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
            return "Record fingerprints of Maven 3 artifacts";
        }
    }
}
