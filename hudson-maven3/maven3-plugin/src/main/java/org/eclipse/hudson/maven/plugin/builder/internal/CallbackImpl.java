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

package org.eclipse.hudson.maven.plugin.builder.internal;

import org.eclipse.hudson.maven.plugin.builder.MavenBuilder;
import org.eclipse.hudson.utils.common.TestAccessible;
import org.eclipse.hudson.maven.model.config.DocumentDTO;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;
import org.eclipse.hudson.maven.model.state.BuildStateDTO;
import org.eclipse.hudson.maven.model.state.ExecutionActivityDTO;
import org.eclipse.hudson.maven.model.state.ExecutionActivityTypeDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;
import org.eclipse.hudson.maven.model.state.RuntimeEnvironmentDTO;
import org.model.hudson.maven.eventspy.common.Callback;
import org.model.hudson.maven.eventspy.common.DocumentReference;

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
 * @since 2.1.0
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
    
    public File getMavenContextDirectory() {
        return mavenContextDirectory;
    }

    public boolean isAborted() {
        return false;
    }

    private void open() {
        recordActivity(ExecutionActivityTypeDTO.STARTED);
    }

    public void close() {
        recordActivity(ExecutionActivityTypeDTO.FINISHED);
        log.info("EventSpy is finished; closing");
    }

    public void setRuntimeEnvironment(final RuntimeEnvironmentDTO env) {
        checkNotNull(env);

        log.debug("Runtime environment captured");
        buildState.setRuntimeEnvironment(env);
    }

    public void setParticipatingProjects(final List<MavenProjectDTO> projects) {
        checkNotNull(projects);

        log.debug("Adding {} participating projects.", projects.size());
        buildState.getParticipatingProjects().clear();
        buildState.getParticipatingProjects().addAll(projects);
    }

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
    
    public void setArtifacts(final Collection<ArtifactDTO> artifacts) {
        buildState.getArtifacts().clear();
        buildState.withArtifacts(artifacts);
    }
    
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

    public DocumentReference getSettingsDocument() {
        return getDocumentContent(owner.getConfig().getSettingsId());
    }

    public DocumentReference getGlobalSettingsDocument() {
        return getDocumentContent(owner.getConfig().getGlobalSettingsId());
    }

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
