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

import org.hudsonci.maven.model.MavenCoordinatesDTO;
import org.hudsonci.maven.model.state.ArtifactDTO;
import org.hudsonci.maven.model.state.ArtifactDTOHelper;
import org.hudsonci.utils.tasks.PerformOperation;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.hudsonci.maven.plugin.artifactrecorder.ArtifactArchiver;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hudsonci.utils.common.Varargs.$;

/**
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class PerformArchiving
    extends PerformOperation<ArtifactArchiver>
{
    private static final String ARTIFACT_TYPE_POM = "pom";
    
    private final Set<ArtifactDTO> artifacts;
    private final boolean includePoms;
    private final boolean deleteOld;

    public PerformArchiving(final ArtifactArchiver owner, final AbstractBuild build, final Launcher launcher,
                            final BuildListener listener, final Set<ArtifactDTO> artifacts,
                            final boolean includePoms, final boolean deleteOld) {
        super(owner, build, launcher, listener);
        this.artifacts = checkNotNull(artifacts);
        this.includePoms = includePoms;
        this.deleteOld = deleteOld;
    }

    protected boolean doExecute() throws Exception {
        if (deleteOld) {
            deleteOldBuildArtifacts();
        }
        
        if (artifacts.isEmpty()) {
            muxlog.info("No Maven 3 artifacts to archive.");
            return true;
        }

        muxlog.info("Archiving Maven 3 artifacts.");
        int count = archiveArtifacts(artifacts);
        muxlog.info("Archived Maven 3 artifacts: {}.", count);

        // Don't stop the build.
        return true;
    }

    private int archiveArtifacts(Collection<ArtifactDTO> artifacts) {
        int count = 0;

        FilePath workspace = build.getWorkspace();
        // Check if workspace exists.
        if (null == workspace) {
            muxlog.error("Missing workspace to archive artifacts from.");
            // TODO: consider this a failure?
            // build.setResult(Result.FAILURE);
            return count;
        }

        try {
            FilePath archiveDirectory = new FilePath(build.getArtifactsDir());
            archiveDirectory.mkdirs();
            log.debug("Archiving to {}", archiveDirectory);

            VirtualChannel channel = workspace.getChannel();
            for (ArtifactDTO artifact : artifacts) {
                if (null != artifact.getCreatedProject() && maybeIncludePom(artifact)) {
                    MavenCoordinatesDTO gav = artifact.getCoordinates();

                    // File information for the artifact.
                    File file = ArtifactDTOHelper.getFile(artifact);
                    // TODO: filter out nulls when generating the artifact list; i.e. at ArtifactRecorder
                    if (file != null) {
                        // Executing on the master; do not try to canonicalize the path since it could have been defined
                        // from a remote file system that is not the same as the masters.  E.g. master being linux, 
                        // remote being windows.
                        // TODO: consider making a FilePath constructor using channel and file and extract the path
                        // so that users don't have to think about differences between canonical, absolute, and path.
                        FilePath source = new FilePath(channel, file.getPath());
                        
                        // Store in archive using a similar format as a Maven repository.
                        FilePath target = archiveDirectory
                            .child(gav.getGroupId()).child(gav.getArtifactId()).child(gav.getVersion())
                            // Resolve source filename using the same node that created the file.  This should 
                            // ensure that the name portion is properly extracted since the file system is the same.
                            .child(source.getName());

                        if(log.isTraceEnabled())
                        {
                            // Separate calls to get more info when there are failures.
                            log.trace("Created path to archive: {} on channel {}", target, target.getChannel());
                            log.trace("Copying FROM {} on channel {} TO {} on channel {}", $(source, source.getChannel(), target, target.getChannel()));
                        }

                        source.copyTo(target);
                        // TODO: catch IOException and continue with rest of artifacts?
                        count++;
                    }
                    else {
                        muxlog.error("Failed to archive Maven 3 artifact {} -" +
                                " unresolved.", gav);
                    }
                }
            }
        }
        // No need to stop the build due to these exceptions.
        catch (IOException e) {
            Util.displayIOException(e, listener);
            muxlog.error("Failed to archive Maven 3 artifacts", e);
        }
        catch (InterruptedException e) {
            muxlog.error("Failed to archive Maven 3 artifacts", e);
        }

        return count;
    }

    private boolean maybeIncludePom(final ArtifactDTO artifact) {
        if (includePoms) {
            return true;
        } else {
            return !ARTIFACT_TYPE_POM.equals(artifact.getCoordinates().getType());
        }
    }

    /**
     * Copied from {@link hudson.tasks.ArtifactArchiver}.
     */
    private void deleteOldBuildArtifacts() {
        AbstractBuild<?, ?> b = build.getProject().getLastCompletedBuild();
        Result bestResultSoFar = Result.NOT_BUILT;
        while (b != null) {
            if (b.getResult().isBetterThan(bestResultSoFar)) {
                bestResultSoFar = b.getResult();
            }
            else {
                // remove old artifacts
                File ad = b.getArtifactsDir();
                if (ad.exists()) {
                    muxlog.info("Deleting old Maven artifacts from {}", b.getDisplayName());
                    try {
                        Util.deleteRecursive(ad);
                    }
                    catch (IOException e) {
                        muxlog.error("Failed to delete old Maven artifacts", e);
                    }
                }
            }
            b = b.getPreviousBuild();
        }
    }
}
