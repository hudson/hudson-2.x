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

package org.eclipse.hudson.maven.plugin.dependencymonitor.internal;

import org.eclipse.hudson.maven.plugin.dependencymonitor.ArtifactsExtractor;
import org.eclipse.hudson.maven.plugin.dependencymonitor.ArtifactsPair;
import org.eclipse.hudson.maven.plugin.dependencymonitor.internal.ProjectArtifactCacheImpl;
import org.eclipse.hudson.service.ProjectService;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import hudson.model.AbstractProject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link ProjectArtifactCacheImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectArtifactCacheImplTest
{
    @Mock
    private ProjectService projectService;

    @Mock
    private ArtifactsExtractor artifactsExtractor;

    private ProjectArtifactCacheImpl cache;

    private MavenCoordinatesDTO a1 = new MavenCoordinatesDTO().withGroupId("a").withArtifactId("a1");

    private MavenCoordinatesDTO a2 = new MavenCoordinatesDTO().withGroupId("a").withArtifactId("a2");

    private MavenCoordinatesDTO a3 = new MavenCoordinatesDTO().withGroupId("a").withArtifactId("a3");

    @Mock
    private AbstractProject project1;

    @Mock
    private AbstractProject project2;

    @Before
    public void setUp() throws Exception {
        cache = new ProjectArtifactCacheImpl(projectService, artifactsExtractor);
    }

    private void assertNoArtifacts(final AbstractProject project) {
        ArtifactsPair artifacts = cache.getArtifacts(project);
        assertNotNull(artifacts);
        assertTrue(artifacts.isEmpty());
    }

    private void assertHasArtifacts(final AbstractProject project) {
        ArtifactsPair artifacts = cache.getArtifacts(project);
        assertNotNull(artifacts);
        assertFalse(artifacts.isEmpty());
    }

    @Test
    public void testUpdateProjectArtifacts() {
        ArtifactsPair artifacts1 = cache.getArtifacts(project1);
        assertNotNull(artifacts1);
        assertNotNull(artifacts1.isEmpty());

        ArtifactsPair artifacts2 = new ArtifactsPair().withProduced(a1).withConsumed(a2);

        cache.updateArtifacts(project1, artifacts2);
        ArtifactsPair artifacts3 = cache.getArtifacts(project1);
        assertNotNull(artifacts3);
        assertThat(artifacts3.produced, hasItem(a1));
        assertThat(artifacts3.consumed, hasItem(a2));

        artifacts2.produced.add(a3);
        cache.updateArtifacts(project1, artifacts2);
        ArtifactsPair artifacts4 = cache.getArtifacts(project1);
        assertNotNull(artifacts4);
        assertThat(artifacts4.produced, hasItems(a1, a3));
        assertThat(artifacts4.consumed, hasItem(a2));
    }

    @Test
    public void testArtifactPairCanNotMutatePostUpdate() {
        ArtifactsPair artifacts1 = new ArtifactsPair().withProduced(a1).withConsumed(a2);
        cache.updateArtifacts(project1, artifacts1);
        artifacts1.produced.add(a3);

        ArtifactsPair artifacts2 = cache.getArtifacts(project1);
        assertNotNull(artifacts2);
        assertThat(artifacts2.produced, not(hasItem(a3)));
    }

    @Test(expected = Exception.class)
    public void testArtifactPairCanNotMutate() {
        ArtifactsPair artifacts1 = new ArtifactsPair().withProduced(a1).withConsumed(a2);
        cache.updateArtifacts(project1, artifacts1);

        ArtifactsPair artifacts2 = cache.getArtifacts(project1);
        artifacts2.produced.add(a3);
    }

    @Test
    public void testProducedArtifacts() {
        Collection<MavenCoordinatesDTO> artifacts1 = cache.getProducedArtifacts(project1);
        assertNotNull(artifacts1);
        assertTrue(artifacts1.isEmpty());

        ArtifactsPair artifacts2 = new ArtifactsPair().withProduced(a1).withConsumed(a2);
        cache.updateArtifacts(project1, artifacts2);

        Collection<MavenCoordinatesDTO> artifacts3 = cache.getProducedArtifacts(project1);
        assertThat(artifacts3, hasItem(a1));
    }

    @Test
    public void testConsumedArtifacts() {
        Collection<MavenCoordinatesDTO> artifacts1 = cache.getConsumedArtifacts(project1);
        assertNotNull(artifacts1);
        assertTrue(artifacts1.isEmpty());

        ArtifactsPair artifacts2 = new ArtifactsPair().withProduced(a1).withConsumed(a2);
        cache.updateArtifacts(project1, artifacts2);

        Collection<MavenCoordinatesDTO> artifacts3 = cache.getConsumedArtifacts(project1);
        assertThat(artifacts3, hasItem(a2));
    }

    @Test
    public void testArtifactProducers() {
        ArtifactsPair artifacts1 = new ArtifactsPair().withProduced(a1).withConsumed(a2);
        cache.updateArtifacts(project1, artifacts1);

        Collection<AbstractProject> projects = cache.getArtifactProducers();
        assertThat(projects, hasItem(project1));
    }

    @Test
    public void testArtifactConsumers() {
        ArtifactsPair artifacts1 = new ArtifactsPair().withProduced(a1).withConsumed(a2);
        cache.updateArtifacts(project1, artifacts1);

        Collection<AbstractProject> projects = cache.getArtifactConsumers();
        assertThat(projects, hasItem(project1));
    }

    @Test
    public void testProducersOf() {
        ArtifactsPair artifacts1 = new ArtifactsPair();
        artifacts1.produced.add(a1);
        artifacts1.consumed.add(a2);
        cache.updateArtifacts(project1, artifacts1);

        Collection<AbstractProject> projects = cache.getProducersOf(a1);
        assertThat(projects, hasItem(project1));
        assertThat(projects, not(hasItem(project2)));
    }

    @Test
    public void testConsumersOf() {
        ArtifactsPair artifacts1 = new ArtifactsPair().withProduced(a1).withConsumed(a2);
        cache.updateArtifacts(project1, artifacts1);

        Collection<AbstractProject> projects = cache.getConsumersOf(a2);
        assertThat(projects, hasItem(project1));
        assertThat(projects, not(hasItem(project2)));
    }

    @Test
    public void testIsProduced() {
        ArtifactsPair artifacts1 = new ArtifactsPair().withProduced(a1).withConsumed(a2);
        cache.updateArtifacts(project1, artifacts1);

        assertTrue(cache.isProduced(a1));
        assertFalse(cache.isProduced(a2));
    }

    @Test
    public void testIsConsumed() {
        ArtifactsPair artifacts1 = new ArtifactsPair().withProduced(a1).withConsumed(a2);
        cache.updateArtifacts(project1, artifacts1);

        assertTrue(cache.isConsumed(a2));
        assertFalse(cache.isConsumed(a1));
    }

    @Test
    public void testPurgeArtifacts() {
        assertNoArtifacts(project1);
        assertNoArtifacts(project2);

        ArtifactsPair artifacts1 = new ArtifactsPair().withProduced(a1).withConsumed(a2);
        cache.updateArtifacts(project1, artifacts1);
        assertHasArtifacts(project1);

        ArtifactsPair artifacts2 = new ArtifactsPair().withProduced(a1).withConsumed(a2);
        cache.updateArtifacts(project2, artifacts2);
        assertHasArtifacts(project2);

        cache.purgeArtifacts(project1);
        assertNoArtifacts(project1);
        assertHasArtifacts(project2);
    }

    @Test
    public void testClear() {
        assertNoArtifacts(project1);
        assertNoArtifacts(project2);

        ArtifactsPair artifacts1 = new ArtifactsPair().withProduced(a1).withConsumed(a2);
        cache.updateArtifacts(project1, artifacts1);
        assertHasArtifacts(project1);

        ArtifactsPair artifacts2 = new ArtifactsPair().withProduced(a1).withConsumed(a2);
        cache.updateArtifacts(project2, artifacts2);
        assertHasArtifacts(project2);

        cache.clear();
        assertNoArtifacts(project1);
        assertNoArtifacts(project2);
    }
}
