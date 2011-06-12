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

package org.hudsonci.maven.plugin.artifactrecorder.internal;

import com.google.common.collect.Sets;
import org.hudsonci.maven.model.state.ArtifactDTO;

import org.hudsonci.maven.plugin.artifactrecorder.ArtifactFingerprinter;
import org.hudsonci.maven.plugin.artifactrecorder.internal.PerformFingerprinting;
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

import static org.hudsonci.maven.model.test.CannedDtos.fakeArtifact;
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
