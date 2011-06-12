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

import org.hudsonci.utils.common.TestAccessible;
import org.hudsonci.maven.eventspy.common.Callback;
import org.hudsonci.maven.eventspy.common.DocumentReference;
import org.hudsonci.maven.model.config.DocumentDTO;
import org.hudsonci.maven.model.state.ArtifactDTO;
import org.hudsonci.maven.model.state.BuildStateDTO;
import org.hudsonci.maven.model.state.ExecutionActivityDTO;
import org.hudsonci.maven.model.state.ExecutionActivityTypeDTO;
import org.hudsonci.maven.model.state.MavenProjectDTO;
import org.hudsonci.maven.model.state.RuntimeEnvironmentDTO;

import org.hudsonci.maven.plugin.builder.MavenBuilder;
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
