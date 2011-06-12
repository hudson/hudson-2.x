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

package org.hudsonci.maven.plugin.builder.internal;

import org.hudsonci.maven.model.state.ArtifactActionDTO;
import org.hudsonci.maven.model.state.ArtifactDTO;
import org.hudsonci.maven.model.state.ArtifactOperationDTO;

import org.hudsonci.maven.plugin.builder.internal.ArtifactRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hudsonci.maven.model.test.CannedDtos.fakeArtifact;
import static org.hudsonci.maven.model.test.CannedDtos.fakeCoordinates;


import static org.hamcrest.Matchers.nullValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * Test for {@link ArtifactRegistry} value setting and merging.
 * 
 * @author Jamie Whitehouse
 */
public class ArtifactRegistryTest
{
    private ArtifactRegistry registry;

    @Before
    public void createRegistry() {
        registry = new ArtifactRegistry();
        assertThat(registry.getAll(), hasSize(0));
    }
    
    @After
    public void verifyRegistry() {
        assertThat(registry.getAll(), hasSize(1));
    }
    
    @Test
    public void updateDoesNotOverwriteExistingRepositoryFile() {
        ArtifactDTO original = fakeArtifact();

        // Null updates should be ignored.
        ArtifactDTO nullUpdate = fakeArtifact().withRepositoryFile(null);
        updateAndVerifyExpectedContents(nullUpdate, original);

        // New (null) attributes should be overwritten.
        original.withRepositoryFile("original.jar");
        updateAndVerifyExpectedContents(original, original);

        // Non-null attributes should not be overwritten.
        ArtifactDTO overwriteUpdate = fakeArtifact().withRepositoryFile("updated.jar");
        updateAndVerifyExpectedContents(overwriteUpdate, original);
    }

    @Test
    public void updateDoesNotOverwriteExistingType() {
        ArtifactDTO original = fakeArtifact();

        // Null updates should be ignored.
        ArtifactDTO nullUpdate = fakeArtifact().withType(null);
        updateAndVerifyExpectedContents(nullUpdate, original);

        // New (null) attributes should be overwritten.
        original.withType("original-type");
        updateAndVerifyExpectedContents(original, original);

        // Non-null attributes should not be overwritten.
        ArtifactDTO overwriteUpdate = fakeArtifact().withType("updated-type");
        updateAndVerifyExpectedContents(overwriteUpdate, original);
        
        assertThat(registry.getAll(), hasSize(1));
    }

    @Test
    public void updateDoesNotOverwriteExistingCreatedProject() {
        ArtifactDTO original = fakeArtifact();

        // Null updates should be ignored.
        ArtifactDTO nullUpdate = fakeArtifact().withCreatedProject(null);
        updateAndVerifyExpectedContents(nullUpdate, original);

        // New (null) attributes should be overwritten.
        original.withCreatedProject("gid:aid:version");
        updateAndVerifyExpectedContents(original, original);

        // Non-null attributes should not be overwritten.
        ArtifactDTO overwriteUpdate = fakeArtifact().withCreatedProject("gid:updated-aid:updated-version");
        updateAndVerifyExpectedContents(overwriteUpdate, original);
    }

    @Test
    public void updateOverwritesSnapshotWithTimestampVersion() {
        // Setup as if artifact was previously installed.
        // When INSTALLED the meta version is not timestamped, just -SNAPSHOT.
        String installedVersion = "1.0-SNAPSHOT";
        ArtifactDTO original = fakeArtifact();
        original.getCoordinates().withVersion(installedVersion);

        // Null updates should be ignored.
        original.getCoordinates().withExpandedMetaVersion(null);
        ArtifactDTO nullUpdate = updateAndVerifyExpectedContents(original, original);
        assertThat(nullUpdate.getCoordinates().getExpandedMetaVersion(), nullValue());

        // SNAPSHOT updates should be ignored
        ArtifactDTO install = fakeArtifact();
        install.getCoordinates().withVersion(installedVersion);
        install.getCoordinates().withExpandedMetaVersion(installedVersion);
        ArtifactDTO installUpdate = updateAndVerifyExpectedContents(install, install);
        assertThat(installUpdate.getCoordinates().getExpandedMetaVersion(), nullValue());

        // Timestamp updates should be applied.
        String deployedVersion = "1.0-20110210.203215-16";
        ArtifactDTO deploy = fakeArtifact();
        deploy.getCoordinates().withVersion(installedVersion);
        deploy.getCoordinates().withExpandedMetaVersion(deployedVersion);
        ArtifactDTO deployUpdate = updateAndVerifyExpectedContents(deploy, deploy);
        assertThat(deployUpdate.getCoordinates().getExpandedMetaVersion(), equalTo(deployedVersion));
    }
    
    @Test
    public void updateMergesExistingActions() {
        // Empty collections should have no effect.
        registry.recordArtifact(fakeArtifact().withActions(Collections.<ArtifactActionDTO>emptyList()));
        assertThat(registry.get(fakeCoordinates()).getActions(), hasSize(0));

        // Collections should be additive.
        ArtifactActionDTO opOne = new ArtifactActionDTO()
                                 .withProjectId(fakeCoordinates().toString())
                                 .withOperation(ArtifactOperationDTO.RESOLVED);
        registry.recordArtifact(fakeArtifact().withActions(opOne));
        assertThat(registry.get(fakeCoordinates()).getActions(), hasSize(1));

        ArtifactActionDTO opTwo = new ArtifactActionDTO()
                                .withProjectId(fakeCoordinates().toString())
                                .withOperation(ArtifactOperationDTO.DOWNLOADED);
        registry.recordArtifact(fakeArtifact().withActions(opTwo));
        assertThat(registry.get(fakeCoordinates()).getActions(), hasSize(2));
    }

    @Test
    public void updateMergesExistingConsumingProjects() {
        // Empty collections should have no effect.
        registry.recordArtifact(fakeArtifact().withConsumingProjects(Collections.<String>emptyList()));
        assertThat(registry.get(fakeCoordinates()).getConsumingProjects(), hasSize(0));

        // Collections should be additive.
        registry.recordArtifact(fakeArtifact().withConsumingProjects("one-consumer"));
        assertThat(registry.get(fakeCoordinates()).getConsumingProjects(), hasSize(1));

        registry.recordArtifact(fakeArtifact().withConsumingProjects("two-consumer"));
        assertThat(registry.get(fakeCoordinates()).getConsumingProjects(), hasSize(2));
    }
    
    @Test
    public void updateMergesExistingDependentProjects() {
        // Empty collections should have no effect.
        registry.recordArtifact(fakeArtifact().withDependentProjects(Collections.<String>emptyList()));
        assertThat(registry.get(fakeCoordinates()).getDependentProjects(), hasSize(0));

        // Collections should be additive.
        registry.recordArtifact(fakeArtifact().withDependentProjects("one-dependent"));
        assertThat(registry.get(fakeCoordinates()).getDependentProjects(), hasSize(1));

        registry.recordArtifact(fakeArtifact().withDependentProjects("two-dependent"));
        assertThat(registry.get(fakeCoordinates()).getDependentProjects(), hasSize(2));
    }

    /**
     * @return the actual artifact retrieved from the registry used in the verification.
     *          Useful to perform other assertions. 
     */
    private ArtifactDTO updateAndVerifyExpectedContents(ArtifactDTO artifactToRecord, ArtifactDTO expectedArtifactContents) {
        assertThat("Artifacts must have matching coordinates", artifactToRecord.getCoordinates(), equalTo(expectedArtifactContents.getCoordinates()));
        registry.recordArtifact(artifactToRecord);
        ArtifactDTO actual = registry.get(expectedArtifactContents.getCoordinates());
        assertThat(actual, equalTo(expectedArtifactContents));
        
        return actual;
    }
}
