/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.internal;

import com.sonatype.matrix.common.TestAccessible;
import com.sonatype.matrix.maven.eventspy.common.Callback;
import com.sonatype.matrix.maven.eventspy.common.DocumentReference;
import com.sonatype.matrix.maven.model.config.DocumentDTO;
import com.sonatype.matrix.maven.model.state.ArtifactDTO;
import com.sonatype.matrix.maven.model.state.BuildStateDTO;
import com.sonatype.matrix.maven.model.state.ExecutionActivityDTO;
import com.sonatype.matrix.maven.model.state.ExecutionActivityTypeDTO;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;
import com.sonatype.matrix.maven.model.state.RuntimeEnvironmentDTO;
import com.sonatype.matrix.maven.plugin.builder.MavenBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.model.AbstractBuild;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link Callback}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Jamie Whitehouse
 * 
 * @since 1.1
 */
public class CallbackImpl
    implements Callback
{
    private static final Logger log = LoggerFactory.getLogger(CallbackImpl.class);

    private final MavenBuilder owner;

    private final AbstractBuild<?,?> build;

    private final BuildStateDTO buildState;

    private final File mavenContextDirectory;

    private final ArtifactRegistry artifactRegistry;

    private long commitDurationToDate = 0;

    public CallbackImpl(final MavenBuilder owner, BuildStateDTO state, final AbstractBuild<?,?> build) {
        this.owner = checkNotNull(owner);
        this.build = checkNotNull(build);
        this.buildState = checkNotNull(state);

        this.mavenContextDirectory = new File(build.getWorkspace().child(".maven").getRemote());

        // TODO: assisted inject
        this.artifactRegistry = new ArtifactRegistry();
        
        open();
    }
    
    @TestAccessible
    public CallbackImpl(final BuildStateDTO buildState) {
        // Currently owner and build aren't used; safe to be null.
        this.owner = null;
        this.build = null;
        this.buildState = checkNotNull(buildState);

        // FIXME: This isn't right, maybe just use a tmp dir
        this.mavenContextDirectory = null;

        // TODO: assisted inject
        this.artifactRegistry = new ArtifactRegistry();

        open();
    }
    
    @Override
    public File getMavenContextDirectory() {
        return mavenContextDirectory;
    }

    @Override
    public boolean isAborted() {
        return false;
    }

    private void open() {
        recordActivity(ExecutionActivityTypeDTO.STARTED);
    }

    @Override
    public void close() {
        recordActivity(ExecutionActivityTypeDTO.FINISHED);
        log.info("EventSpy is finished; closing");
    }

    @Override
    public void setRuntimeEnvironment(final RuntimeEnvironmentDTO env) {
        checkNotNull(env);

        log.debug("Runtime environment captured");
        buildState.setRuntimeEnvironment(env);
    }

    @Override
    public void setParticipatingProjects(final List<MavenProjectDTO> projects) {
        checkNotNull(projects);

        log.debug("Adding {} participating projects.", projects.size());
        buildState.getParticipatingProjects().clear();
        buildState.getParticipatingProjects().addAll(projects);
    }

    @Override
    public void updateParticipatingProject(final MavenProjectDTO project) {
        checkNotNull(project);

        log.debug("Updating participating project: {}.", project.getId());

        for (ListIterator<MavenProjectDTO> iterator = buildState.getParticipatingProjects().listIterator(); iterator.hasNext();) {
            if (iterator.next().getCoordinates().equals(project.getCoordinates())) {
                iterator.set(project);
                // Assume there is only one match since using GAV which should be unique
                break;
            }
        }
    }
    
    @Override
    public void setArtifacts(final Collection<ArtifactDTO> artifacts) {
        buildState.getArtifacts().clear();
        buildState.withArtifacts(artifacts);
    }
    
    @Override
    public void addArtifacts(final Collection<ArtifactDTO> artifacts) {
        long start = System.currentTimeMillis();

        for (ArtifactDTO artifactDTO : artifacts) {
            artifactRegistry.recordArtifact(artifactDTO);
        }
        int count = commitArtifacts();
        long duration = System.currentTimeMillis() - start;
        
        commitDurationToDate += duration;
        
        log.debug("Committed {} artifacts in {}ms of cumulative {}ms", new Object[] {count,duration,commitDurationToDate});
    }

    @Override
    public DocumentReference getSettingsDocument() {
        return getDocumentContent(owner.getConfig().getSettingsId());
    }

    @Override
    public DocumentReference getGlobalSettingsDocument() {
        return getDocumentContent(owner.getConfig().getGlobalSettingsId());
    }

    @Override
    public DocumentReference getToolChainsDocument() {
        return getDocumentContent(owner.getConfig().getToolChainsId());
    }

    /**
     * Returns a document reference or null if not found (or document content is blank).
     */
    private DocumentReference getDocumentContent(final String id) {
        final DocumentDTO document = owner.getDocument(id);
        if (document != null) {
            String content = document.getContent();

            // Only return document that actually contains real content
            if (content != null && content.trim().length() != 0) {
                return new DocumentReference(id, content);
            }
        }
        return null;
    }

    private int commitArtifacts() {
        Collection<ArtifactDTO> artifactsToDate = artifactRegistry.getAll();
        setArtifacts(artifactsToDate);
        return artifactsToDate.size();
    }
    
    private void recordActivity(final ExecutionActivityTypeDTO activityType) {
        buildState.getExecutionActivities().add(
            new ExecutionActivityDTO().withType(activityType).withTimestamp(new Date()));
    }
}
