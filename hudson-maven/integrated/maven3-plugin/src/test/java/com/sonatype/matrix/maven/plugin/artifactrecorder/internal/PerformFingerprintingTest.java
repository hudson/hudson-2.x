/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.artifactrecorder.internal;

import com.google.common.collect.Sets;
import com.sonatype.matrix.maven.model.state.ArtifactDTO;
import com.sonatype.matrix.maven.plugin.artifactrecorder.ArtifactFingerprinter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.FingerprintMap;
import hudson.model.StreamBuildListener;
import hudson.util.NullStream;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static com.sonatype.matrix.maven.model.test.CannedDtos.fakeArtifact;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PerformFingerprintingTest
{
    @Mock
    private ArtifactFingerprinter owner;

    @Mock
    private AbstractBuild build;

    @Mock
    private Launcher launcher;

    private FingerprintMap fingerprintMap;

    private BuildListener listener;

    @Before
    public void setUp() throws Exception {
        // Need a real listener to log against since there's too much to fake out.
        listener = new StreamBuildListener(new NullStream());
        fingerprintMap = new FingerprintMap();
    }

    @Test
    public void noFingerprintActionWhenNoArtifacts() throws InterruptedException, IOException {
        new PerformFingerprinting(owner, build, launcher, listener, 0, Collections.<ArtifactDTO> emptySet(),
            fingerprintMap).execute();

        verify(build, never()).addAction(any(Action.class));
    }

    @Test
    public void isResilientToNullFiles() throws InterruptedException, IOException {
        Set<ArtifactDTO> artifacts = Sets.newHashSet(new ArtifactDTO().withRepositoryFile(null));
        new PerformFingerprinting(owner, build, launcher, listener, 1, artifacts, fingerprintMap).execute();

        verify(build, never()).addAction(any(Action.class));
    }

    @Ignore("Too many internal dependencies to test without restructuring.")
    @Test
    public void fingerprintActionAddedToBuild() throws InterruptedException, IOException {
        Set<ArtifactDTO> artifacts = Sets.newHashSet(fakeArtifact());
        new PerformFingerprinting(owner, build, launcher, listener, 1, artifacts, fingerprintMap).execute();

        verify(build).addAction(any(Action.class));
    }
    
    // Failure cases:
    // getFileToFingerprint() is null
    // getFileToFingerprint() path is not present (for example it was deleted before FPing occured
    // fingerprint() FilePath...digest throws documented exceptions
    // fingerprint() fpMap.getOrCreate() throws documented exception
    // NOTE: ^^^ docs say it will never return null; is that correct?  If so
    // the null check and log can be removed.
    
    // Good cases:
    // fp is owned by this build (where the artifact was created by this build)
    // fp is not owned by this build
    // fp is created and this build is associated with it
    // fp is in the action associated with the build

}
