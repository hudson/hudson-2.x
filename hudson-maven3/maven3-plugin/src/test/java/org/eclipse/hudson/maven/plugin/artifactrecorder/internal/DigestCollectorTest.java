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

import com.google.common.collect.Sets;

import org.eclipse.hudson.maven.plugin.artifactrecorder.internal.DigestCollector;
import org.eclipse.hudson.maven.plugin.artifactrecorder.internal.DigestRecord;
import org.eclipse.hudson.utils.io.FileUtil;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import hudson.FilePath;
import hudson.model.AbstractBuild;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.eclipse.hudson.maven.model.test.CannedDtos.fakeArtifact;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link DigestCollector}.
 * 
 * Unfortunately {@link AbstractBuild#getWorkspace()} is final and cannot be mocked.  These tests bypass the Callable
 * and test the internals for digesting.
 * 
 * @author Jamie Whitehouse
 */
@RunWith(MockitoJUnitRunner.class)
public class DigestCollectorTest
{
    @Ignore("AbstractBuild final methods cannot be mocked.")
    @Test
    public void digestWithCallable() throws InterruptedException, IOException {
        String pathToTestFileForDigesting = FileUtil.getResourceAsFile(getClass(), getClass().getSimpleName() + ".class").getAbsolutePath();
        ArtifactDTO artifact = fakeArtifact().withRepositoryFile( pathToTestFileForDigesting );
        
        // If only build.getWorkspace() wasn't final!
        AbstractBuild build = mock(AbstractBuild.class);
        when(build.getWorkspace()).thenReturn(new FilePath(new File(".")));
        
        DigestCollector collector = new DigestCollector(build, Sets.newHashSet(artifact));
        DigestRecord digest = collector.collect().get(0);
        
        // Any non-null value for the digest is valid enough for this test.
        assertThat(digest.getArtifact(), equalTo(artifact));
        assertThat(digest.getDigest(), notNullValue());
    }
    
    @Test
    public void noDigestsCreatedWhenNoArtifacts() throws InterruptedException {
        DigestCollector collector = new DigestCollector(mock(AbstractBuild.class), Collections.<ArtifactDTO>emptySet());
        
        List<DigestRecord> digests = collector.collectFromNode();
        
        assertThat(digests, equalTo(Collections.<DigestRecord>emptyList()));
    }
    
    @Test
    public void noDigestCreatedForNullFilePath() throws InterruptedException {
        ArtifactDTO artifactWithoutFile = fakeArtifact();
        DigestCollector collector = new DigestCollector(mock(AbstractBuild.class), Sets.newHashSet(artifactWithoutFile));

        DigestRecord digest = collector.collectFromNode().get(0);
        
        assertThat(digest.getArtifact(), equalTo(artifactWithoutFile));
        assertThat(digest.getDigest(), nullValue());
    }
    
    @Test
    public void noDigestCreatedForUnknownFilePath() throws InterruptedException {
        ArtifactDTO artifactWithUnknownPath = fakeArtifact().withRepositoryFile( "path/to/nowhere/" );
        DigestCollector collector = new DigestCollector(mock(AbstractBuild.class), Sets.newHashSet(artifactWithUnknownPath));

        DigestRecord digest = collector.collectFromNode().get(0);

        assertThat(digest.getArtifact(), equalTo(artifactWithUnknownPath));
        assertThat(digest.getDigest(), nullValue());
    }
    
    @Test
    public void digestCreatedForValidFilePath() throws InterruptedException {
        File file = FileUtil.getResourceAsFile(getClass(), getClass().getSimpleName() + ".class");
        String pathToTestFileForDigesting = file.getAbsolutePath();
        ArtifactDTO artifact = fakeArtifact().withRepositoryFile( pathToTestFileForDigesting );
        DigestCollector collector = new DigestCollector(mock(AbstractBuild.class), Sets.newHashSet(artifact));
        
        DigestRecord digest = collector.collectFromNode().get(0);

        // Any non-null value for the digest is valid enough for this test.
        assertThat(digest.getArtifact(), equalTo(artifact));
        assertThat(digest.getFilename(), equalTo(file.getName()));
        assertThat(digest.getDigest(), notNullValue());
    }
}
