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

package org.hudsonci.maven.eventspy_30.recorder;

import org.hudsonci.utils.common.TestAccessible;
import org.hudsonci.maven.model.MavenCoordinatesDTO;
import org.hudsonci.maven.model.state.ArtifactActionDTO;
import org.hudsonci.maven.model.state.ArtifactDTO;
import org.hudsonci.maven.model.state.ArtifactOperationDTO;
import org.hudsonci.maven.model.state.BuildResultDTO;
import org.hudsonci.maven.model.state.MavenProjectDTO;
import org.hudsonci.maven.model.state.MavenProjectDTOHelper;

import org.apache.maven.execution.BuildSummary;
import org.apache.maven.project.MavenProject;
import org.hudsonci.maven.eventspy.common.Callback;
import org.hudsonci.maven.eventspy_30.MavenArtifactConverter;
import org.hudsonci.maven.eventspy_30.MavenProjectConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.graph.Dependency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hudsonci.maven.eventspy_30.MavenProjectConverter.convertMavenProject;
import static org.hudsonci.maven.eventspy_30.MavenProjectConverter.updateWithBuildResult;
import static org.hudsonci.maven.eventspy_30.MavenProjectConverter.updateWithBuildSummary;

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
