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

import org.eclipse.hudson.maven.eventspy_30.MavenArtifactConverter;
import org.eclipse.hudson.maven.eventspy_30.MavenProjectConverter;
import org.eclipse.hudson.maven.model.state.MavenProjectDTOHelper;
import org.eclipse.hudson.utils.common.TestAccessible;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import org.eclipse.hudson.maven.model.state.ArtifactActionDTO;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;
import org.eclipse.hudson.maven.model.state.ArtifactOperationDTO;
import org.eclipse.hudson.maven.model.state.BuildResultDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

import org.apache.maven.execution.BuildSummary;
import org.apache.maven.project.MavenProject;
import org.model.hudson.maven.eventspy.common.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.graph.Dependency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.eclipse.hudson.maven.eventspy_30.MavenProjectConverter.convertMavenProject;
import static org.eclipse.hudson.maven.eventspy_30.MavenProjectConverter.updateWithBuildResult;
import static org.eclipse.hudson.maven.eventspy_30.MavenProjectConverter.updateWithBuildSummary;

/**
 * Records and processes significant build events.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class BuildRecorder
{
    private static final Logger log = LoggerFactory.getLogger(BuildRecorder.class);
    private MavenCoordinatesDTO resolvingProjectCoordinates;
    private final Callback callback;

    private String executingProjectId;
    
    private int dependencyArtifactCount = 0;
    private int recordedArtifactCount = 0;
    private long commitDurationToDate = 0;
    private ArrayList<ArtifactDTO> recordedArtifacts = new ArrayList<ArtifactDTO>(350); // Rough guess capacity based on permodule analysis of Hudson. 

    /**
     * @param callback to transfer build information to
     */
    public BuildRecorder(final Callback callback) {
        this.callback = callback;
    }

    public void recordArtifactAction(final ArtifactDTO artifact, final ArtifactOperationDTO operation, final File file) {
        checkNotNull(artifact);
        checkNotNull(operation);
        // A null file is acceptable; occurs when not found or a transfer error.

        // Capture all types, including poms since these can be used as a
        // dependency with scope=import and we'll want to fingerprint them.
        
        artifact.withRepositoryFile(resolveFile(file))
                   .withActions(new ArtifactActionDTO().withProjectId(executingProjectId)
                                                       .withOperation(operation));

        // Artifact creating projects are not consumers.
        if (ArtifactOperationDTO.INSTALLED.equals(operation)) {
            artifact.withCreatedProject(executingProjectId);
        }
        else {
            artifact.withConsumingProjects(executingProjectId);
        }

        recordArtifact(artifact);
    }

    @Deprecated
    public void recordArtifactAction(final MavenCoordinatesDTO coordinates,
                                     final ArtifactOperationDTO operation,
                                     final File file,
                                     final String artifactType) {
        checkNotNull(coordinates);
        checkNotNull(operation);
        // A null file is acceptable; occurs when not found or a transfer error.

        // Capture all types, including poms since these can be used as a
        // dependency with scope=import and we'll want to fingerprint them.
        ArtifactDTO artifact = new ArtifactDTO()
            .withCoordinates(coordinates)
            .withType(artifactType)
            .withRepositoryFile(resolveFile(file))
            .withActions(new ArtifactActionDTO().withProjectId(executingProjectId)
                                                .withOperation(operation));

        // Artifact creating projects are not consumers.
        if (ArtifactOperationDTO.INSTALLED.equals(operation)) {
            artifact.withCreatedProject(executingProjectId);
        }
        else {
            artifact.withConsumingProjects(executingProjectId);
        }

        recordArtifact(artifact);
    }

    /**
     * Resolves the file to a path or returns null if not possible, rather than throwing an exception.
     * 
     * Currently only the ArtifactFingerprinter uses the path, and it checks for nulls and records errors.
     * 
     * I'd rather have the rest of the artifact info recorded and no fingerprint than having the exception cause all
     * info to be lost.
     */
    private String resolveFile(final File file) {
        String path = null;
        if (file != null) {
            try {
                // Note: this must be performed on the actual node the data is being collected on rather than from the
                // callback running on the master.
                path = file.getCanonicalPath();
                log.debug("Computed artifact path for {} as canonicalPath {}", file.getName(), path);
            }
            catch (IOException e) {
                // Treat exception the same as file not found and return null.
                log.error("File could not be resolved", e);
            }
        }
        return path;
    }

    /**
     * Synchronizes recording of artifacts.
     */
    public void recordArtifact(final ArtifactDTO artifact) {
        recordedArtifacts.add(checkNotNull(artifact));
        
        recordedArtifactCount++;
    }

    public void recordDependencyResolutionStarted(final MavenProject mavenProject) {
        if (mavenProject != null) {
            log.debug("Requested dependencies for {}", mavenProject);
            setResolvingProject(mavenProject);
        }
        else {
            log.debug("Requested dependencies for {}", "a non-project");
            resetResolvingProject();
        }
    }

    public void recordDependencyResolutionFinished(final List<Dependency> resolvedDependencies) {
        checkNotNull(resolvedDependencies);
        log.debug("Recording dependencies for {}:", resolvingProjectCoordinates);
        for (Dependency dependency : resolvedDependencies) {
            log.debug("    {}", dependency.toString());

            if (isResolvingProjectRequests()) {
                ArtifactDTO artifactDto = MavenArtifactConverter.convertAetherArtifact(dependency.getArtifact());
                artifactDto.withDependentProjects(MavenProjectDTOHelper.asId(resolvingProjectCoordinates));

                recordArtifact(artifactDto);
                dependencyArtifactCount++;
            }
        }
    }

    public void recordSessionFinished(final List<MavenProjectDTO> participatingProjects) {
        // Record project/module info.
        // TODO: Do we need a sanity check that the in progress collected
        // projects match the final list of projects?
        callback.setParticipatingProjects(participatingProjects);

        // This is as close to the Callback.close event as we're likely to get, at the moment.
        // Just in case artifacts were recorded outside of the last project/module build.
        commitArtifacts();
    }

    private boolean isResolvingProjectRequests() {
        return resolvingProjectCoordinates != null;
    }

    private void setResolvingProject(final MavenProject mavenProject) {
        resolvingProjectCoordinates = MavenProjectConverter.asCoordinates(mavenProject);
    }

    private void resetResolvingProject() {
        resolvingProjectCoordinates = null;
    }

    public void recordSessionStarted(final List<MavenProject> projects) {
        List<MavenProjectDTO> participatingProjects = new ArrayList<MavenProjectDTO>();

        for (MavenProject project : projects) {
            MavenProjectDTO projectDTO = convertMavenProject(project);
            updateWithBuildResult(projectDTO, BuildResultDTO.SCHEDULED);
            participatingProjects.add(projectDTO);
        }

        callback.setParticipatingProjects(participatingProjects);
    }

    public void recordProjectStarted(final MavenProject project) {
        MavenProjectDTO projectDTO = convertMavenProject(project);
        updateWithBuildResult(projectDTO, BuildResultDTO.BUILDING);

        updateProject(projectDTO);
    }

    public void recordProjectFinished(final MavenProject project, final BuildSummary buildSummary) {
        MavenProjectDTO projectDTO = convertMavenProject(project);
        updateWithBuildSummary(projectDTO, buildSummary);

        updateProject(projectDTO);
    }

    // Moved from CallbackImpl.updateParticipatingProject
    private void updateProject(final MavenProjectDTO project) {
        callback.updateParticipatingProject(project);

        if (project.getBuildSummary() != null && BuildResultDTO.BUILDING.equals(project.getBuildSummary().getResult())) {
            executingProjectId = project.getId();
            log.debug("Current executing project: {}.", executingProjectId);
        }
        else {
            // No point in committing artifacts for newly started projects because the contents should not be that
            // much different than the previously finished project and it's a waste of traffic.
            commitArtifacts();
        }
    }

    @TestAccessible
    List<ArtifactDTO> getRecordedArtifacts() {
        return recordedArtifacts;
    }
    
    private void commitArtifacts() {
        long start = System.currentTimeMillis();
        callback.addArtifacts(recordedArtifacts);
        // Takes advantage of the callback being remote and de/serialization breaking the reference to the recordedArtifacts.
        recordedArtifacts.clear();
        long duration = System.currentTimeMillis() - start;
        
        commitDurationToDate += duration;
        log.debug("Committed {} artifacts in {}ms of cumulative {}ms: {} dependencies of {} recorded", new Object[] {
            recordedArtifacts.size(),duration,commitDurationToDate,dependencyArtifactCount, recordedArtifactCount});
    }
}
