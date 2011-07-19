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

package org.eclipse.hudson.maven.plugin.artifactrecorder.internal;

import org.eclipse.hudson.maven.model.MavenCoordinatesDTOHelper.RenderStyle;
import org.eclipse.hudson.maven.model.state.ArtifactDTOHelper;
import org.eclipse.hudson.maven.plugin.artifactrecorder.ArtifactFingerprinter;
import org.eclipse.hudson.utils.common.Varargs;
import org.eclipse.hudson.utils.tasks.PerformOperation;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Fingerprint;
import hudson.model.FingerprintMap;
import hudson.tasks.Fingerprinter.FingerprintAction;
import hudson.tasks.Messages;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class PerformFingerprinting
    extends PerformOperation<ArtifactFingerprinter>
{
    private final Set<ArtifactDTO> artifacts;
    private final FingerprintMap registry;
    private final int numberOfBuilders;

    /**
     * Fingerprints the list of artifacts and attaches them to the build as an {@link Action}.
     *
     * The artifact list can be a subset of all the artifacts from the build.
     *
     * @param build to attach the action to
     * @param numberOfBuilders 
     * @param artifacts to collect digests for
     * @param registry to record fingerprints in for understanding cross project consumption
     */
    public PerformFingerprinting(final ArtifactFingerprinter owner, final AbstractBuild build, final Launcher launcher,
                                 final BuildListener listener, final int numberOfBuilders, final Set<ArtifactDTO> artifacts, final FingerprintMap registry) {
        super(owner, build, launcher, listener);
        this.numberOfBuilders = numberOfBuilders;
        this.artifacts = artifacts;
        this.registry = registry;
    }

    protected boolean doExecute() throws Exception {
        if (artifacts.isEmpty()){
            muxlog.info("No artifacts to fingerprint.");
            return true;
        }
        
        muxlog.info("Recording Maven 3 artifact fingerprints.");
        
        FilePath workspace = build.getWorkspace();
        // Check if workspace exists.
        if (null == workspace) {
            muxlog.error("Missing workspace to record fingerprints from.");
            // TODO: consider this a failure?
            //build.setResult(Result.FAILURE);
            return true;
        }
        
        Map<String, String> recordedFingerprints = new HashMap<String, String>(artifacts.size());
        try {
            List<DigestRecord> digests = new DigestCollector(build,artifacts).collect();
            recordFingerprints(build, digests, recordedFingerprints);
        }
        catch (IOException e) {
            muxlog.error("Fingerprinting failed.", e);
        }
        catch(InterruptedException e) {
            muxlog.error("Fingerprinting aborted.",e);
        }
        
        // TODO: consider it a failure if not all artifacts fingerprinted?
        // Note there can be duplicate artifact files/coords from different builders but the artifacts are not equal
        // due to other object attributes.  
        muxlog.info("Recorded Maven 3 artifact fingerprints: {} files of {} " +
                "build artifacts for {} Maven builders.", Varargs.va( recordedFingerprints.size(), artifacts.size(), numberOfBuilders ) );

        if( ! recordedFingerprints.isEmpty() )
        {
            FingerprintAction.add(build, recordedFingerprints);
        }
        
        // Don't stop the build.
        return true;
    }

    private void recordFingerprints(final AbstractBuild build, final List<DigestRecord> digests, final Map<String, String> recordedFingerprints) throws IOException {
        for (DigestRecord digestRecord : digests) {
            String digest = digestRecord.getDigest();
            ArtifactDTO artifact = digestRecord.getArtifact();

            // The file can be null, if that's the case there should be no digest for the artifact.
            File file = ArtifactDTOHelper.getFile(artifact);

            // Collector couldn't create digest.
            if (null == digest) {
                // I don't consider this a build failure, but if you do then:
                // build.setResult(Result.FAILURE);
                muxlog.debug("{} at {}", Messages.Fingerprinter_FailedFor(artifact.getCoordinates()), file);
                continue;
            }

            // Use coordinates as the displayable string of a fingerprint to disambiguate from other files with the 
            // same base filename.  Using just the groupId is not specific enough in some cases, e.g. tools.jar belongs 
            // to the com.sun gid but the file does not have the version number in it, hence using the full coordinates
            // ensures different tools.jar are unique for the purposes of fingerprint identifiers.
            // Note: The Freestyle Fingerprinter uses the complete path to the file from the workspace root. This 
            // seems excessive in the context of a Maven build, especially if the file is in a shared local repo.
            String fileIdentifier = String.format("%s [%s]", artifact.getCoordinates().toString(RenderStyle.GATCV_OPTIONAL), digestRecord.getFilename());

            // Can throw IOException and IllegalStateException.
            // TODO: consider catching and continuing recording the rest of the fingerprints.
            // Though chances are if one IOException occurred it will occur for the rest of the fingerpriting.
            Fingerprint fingerprint = registry.getOrCreate(maybeMadeByThisBuild(artifact), fileIdentifier, digest);

            // Record that this build has used the artifact.
            fingerprint.add(build);

            // Add to recorded for use in the FingerprintAction.
            // getHashString should be the same as the digest that was just computed
            // but use fingerprints information in case the underlying format changes.
            recordedFingerprints.put(fileIdentifier, fingerprint.getHashString());
        }
    }

    /**
     * Determines if the artifact was created by this build.
     *
     * Matches the expectations of the {@link FingerprintMap#getOrCreate(AbstractBuild, String, String)}.
     *
     * @return the build if it created the artifact, null otherwise.
     */
    private AbstractBuild maybeMadeByThisBuild( final ArtifactDTO artifact )
    {
        // Nothing special needed to match the build to the artifact since the artifact has been retrieved from the
        // build object already.
        return ( artifact.getCreatedProject() == null ? null : build );
    }
}
