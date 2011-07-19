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

package org.eclipse.hudson.maven.eventspy_30.recorder;

import org.eclipse.hudson.maven.model.state.ArtifactDTO;
import com.google.common.collect.ImmutableList;

import org.eclipse.hudson.maven.eventspy_30.recorder.BuildRecorder;
import org.eclipse.hudson.maven.model.state.MavenProjectDTOHelper;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import org.eclipse.hudson.maven.model.state.ArtifactOperationDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;
import org.eclipse.hudson.maven.model.state.RuntimeEnvironmentDTO;

import org.apache.maven.project.MavenProject;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.model.hudson.maven.eventspy.common.Callback;
import org.model.hudson.maven.eventspy.common.DocumentReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.eclipse.hudson.maven.model.test.CannedDtos.fakeArtifact;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

/**
 * Tests for {@link BuildRecorder} functions related to tracking artifacts.
 * 
 * @author Jamie Whitehouse
 */
//@Ignore("Test needs updating to match slight rework of Callback/BuildRecorder interaction.")
public class BuildRecorderArtifactRecordingTest
{
    private BuildRecorder recorder;

    private CallbackStub callback;

    @Before
    public void configure() {
        callback = new CallbackStub();
        recorder = new BuildRecorder(callback);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void recordedArtifactPassedToCallbackWhenSessionFinished() {
        ArtifactDTO artifact = fakeArtifact();

        recorder.recordArtifact(artifact);
        recorder.recordSessionFinished(null);

        assertThat(getCallbackArtifacts(), hasItem(artifact));
    }

    /**
     * Merging happens with the ArtifactRegistry on the master in the Callback.
     */
    @Test
    public void artifactsShouldNotBeAggregatedOrMerged() {
        recorder.recordArtifactAction(fakeArtifact(), ArtifactOperationDTO.DOWNLOADED, new File("."));
        recorder.recordArtifactAction(fakeArtifact(), ArtifactOperationDTO.RESOLVED, new File("."));
        recorder.recordSessionFinished(null);

        List<ArtifactDTO> artifacts = getCallbackArtifacts();
        assertThat(artifacts, hasSize(2));
    }

    /**
     * From Benjamin Bentmann: Aether artifact resolution can succeed or fail, the event is fired in both cases to
     * signal the end of the resolution. The RepositoryEvent.getException() should indicate success or failure. From
     * Aether's point of view, there are only two reasons for resolution failures, not found or transfer issue. Checking
     * the artifact file is another way to detect failure/success, by definition, an artifact is considered resolved if
     * and only if it has a file.
     * 
     * From Jamie: We need to accept null Files.
     */
    @Test
    public void unresolvedArtifactIsAcceptable() {
        File nullFile = null;
        recorder.recordArtifactAction(fakeArtifact(), ArtifactOperationDTO.NOT_FOUND, nullFile);
        // JUnit will error if an unexpected exception is thrown.

        assertThat(recorder.getRecordedArtifacts().get(0).getActions(), hasSize(1));
    }

    @Test
    public void projectStartedDoesNotCommitArtifacts() {
        // Make a record to confirm it's not committed when starting a project.
        recorder.recordArtifactAction(fakeArtifact(), ArtifactOperationDTO.RESOLVED, new File("."));
        
        MavenProject project = createBuildingProject(createBuildingCoordinates());
        recorder.recordProjectStarted(project);
        
        assertThat(getCallbackArtifacts(), Matchers.<ArtifactDTO> empty());
    }
    
    @Test
    public void projectFinishedCommitsArtifacts() {
        MavenProject project = createBuildingProject(createBuildingCoordinates());
        recorder.recordProjectStarted(project);
        recorder.recordArtifactAction(fakeArtifact(), ArtifactOperationDTO.RESOLVED, new File("."));
        recorder.recordProjectFinished(project, null);
        
        assertThat(getCallbackArtifacts(), hasSize(1));
    }
    
    @Test
    public void currentBuildingProjectHasDependenciesTracked() {
        MavenCoordinatesDTO coordinates = buildProjectWithArtifactOperation(ArtifactOperationDTO.DOWNLOADED);
        ArtifactDTO committedArtifact = recorder.getRecordedArtifacts().get(0);

        assertThat(committedArtifact.getConsumingProjects(), contains(MavenProjectDTOHelper.asId(coordinates)));
        assertThat(committedArtifact.getDependentProjects(), Matchers.<String> empty());
    }

    @Test
    public void artifactInstallOperationRecordsCreatorButNotConsumer() {
        MavenCoordinatesDTO coordinates = buildProjectWithArtifactOperation(ArtifactOperationDTO.INSTALLED);
        ArtifactDTO committedArtifact = recorder.getRecordedArtifacts().get(0);

        assertThat(committedArtifact.getActions(), hasSize(1));

        assertThat(committedArtifact.getCreatedProject(), equalTo(MavenProjectDTOHelper.asId(coordinates)));

        assertThat(committedArtifact.getConsumingProjects(), Matchers.<String> empty());
        assertThat(committedArtifact.getDependentProjects(), Matchers.<String> empty());
    }
    
    // TODO: should projects deploying be considered the creator?
    // E.g. what if this project is just deploying other artifacts?
    // How does this work with assembly attach and build helper like plugins?
    @Test
    public void artifactDeployOperationIsRecordedAsConsumerNotCreator() {
        buildProjectWithArtifactOperation(ArtifactOperationDTO.DEPLOYED);
        ArtifactDTO committedArtifact = recorder.getRecordedArtifacts().get(0);

        assertThat(committedArtifact.getActions(), hasSize(1));
        assertThat(committedArtifact.getConsumingProjects(), hasSize(1));
        assertThat(committedArtifact.getDependentProjects(), Matchers.<String> empty());
    }

    @Test
    public void consumingOperationsHaveProjectRecordedAsConsumer() {
        MavenCoordinatesDTO buildingProject = buildProjectWithArtifactOperation(ArtifactOperationDTO.DEPLOYED);
        assertThat(recorder.getRecordedArtifacts(), hasSize(1));
        assertThat(recorder.getRecordedArtifacts().get(0).getConsumingProjects(), contains(MavenProjectDTOHelper.asId(buildingProject)));
                
        buildProjectWithArtifactOperation(ArtifactOperationDTO.RESOLVED);
        assertThat(recorder.getRecordedArtifacts(), hasSize(2));
        assertThat(recorder.getRecordedArtifacts().get(1).getConsumingProjects(), contains(MavenProjectDTOHelper.asId(buildingProject)));
    }
    
    private List<ArtifactDTO> getCallbackArtifacts() {
        return callback.getArtifacts();
    }

    private MavenCoordinatesDTO buildProjectWithArtifactOperation(final ArtifactOperationDTO operation) {
        MavenCoordinatesDTO coordinates = createBuildingCoordinates();
        MavenProject project = createBuildingProject(coordinates);
        recorder.recordProjectStarted(project);
        recorder.recordArtifactAction(fakeArtifact(), operation, new File("."));
        return coordinates;
    }

    private MavenProject createBuildingProject(MavenCoordinatesDTO coordinates) {
        MavenProject project = new MavenProject();
        project.setGroupId(coordinates.getGroupId());
        project.setArtifactId(coordinates.getArtifactId());
        project.setPackaging(coordinates.getType());
        project.setVersion(coordinates.getVersion());
        return project;
    }

    private MavenCoordinatesDTO createBuildingCoordinates() {
        return new MavenCoordinatesDTO().withGroupId("fake-id").withArtifactId("building").withType("jar").withVersion("test-version");
    }
    
    private class CallbackStub implements Callback {
        private final List<ArtifactDTO> capturedArtifacts = new ArrayList<ArtifactDTO>();

        public List<ArtifactDTO> getArtifacts() {
            return capturedArtifacts;
        }

        public void addArtifacts(final Collection<ArtifactDTO> artifacts) {
            capturedArtifacts.addAll(ImmutableList.copyOf(artifacts));
        }

        public void setParticipatingProjects(List<MavenProjectDTO> projects) {
        }
        
        public void updateParticipatingProject(MavenProjectDTO project) {
            // Do nothing but allow method to pass for test.
        }

        public File getMavenContextDirectory() {
            throw new UnsupportedOperationException("Not implemented");
        }

        public boolean isAborted() {
            throw new UnsupportedOperationException("Not implemented");
        }

        public void close() {
            throw new UnsupportedOperationException("Not implemented");
        }

        public void setRuntimeEnvironment(RuntimeEnvironmentDTO env) {
            throw new UnsupportedOperationException("Not implemented");
        }

        public void setArtifacts(Collection<ArtifactDTO> artifacts) {
            throw new UnsupportedOperationException("Not implemented");
        }

        public DocumentReference getSettingsDocument() {
            throw new UnsupportedOperationException("Not implemented");
        }

        public DocumentReference getGlobalSettingsDocument() {
            throw new UnsupportedOperationException("Not implemented");
        }

        public DocumentReference getToolChainsDocument() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
