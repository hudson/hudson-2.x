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

import org.hudsonci.utils.common.TestAccessible;
import org.hudsonci.maven.model.state.ArtifactDTO;
import org.hudsonci.maven.model.state.ArtifactDTOHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.AbstractBuild;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Collect {@link ArtifactDTO} digests.
 *  
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class DigestCollector implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(DigestCollector.class);

    private final Set<ArtifactDTO> artifacts;

    private final transient AbstractBuild build;

    /**
     * Creates a collector for the build and corresponding list of artifacts from that build.
     * 
     * The artifact list can be a subset of all the artifacts from the build.
     * 
     * @param build to determine what node to find artifacts at 
     * @param artifacts the artifacts to collect digests for
     */
    public DigestCollector(AbstractBuild build, Set<ArtifactDTO> artifacts) {
        this.build = checkNotNull(build);
        this.artifacts = checkNotNull(artifacts);
        log.debug("Configured to collect {} artifacts from build {}", artifacts.size(), build);
    }
    
    /**
     * The caller should check for null digests and take appropriate action.
     * 
     * @return a Map of ArtifactDTO to calculated digests in String form.
     * @throws IOException
     * @throws InterruptedException
     */
    public List<DigestRecord> collect() throws IOException, InterruptedException {
        FilePath workspace = build.getWorkspace();

        // Check if workspace exists.
        if(null == workspace) {
            throw new IOException("Missing node to collect artifact digests from.");
        }

        // Extracts the workspace or node or whatever from the build to look up the artifacts paths and digest.
        // See Launcher.getComputer() for suggestions on how to get the current computer within a build.
        return workspace.act(new FileCallable<List<DigestRecord>>()
        {
            private static final long serialVersionUID = 1L;

            public List<DigestRecord> invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                return collectFromNode();
            }
        });
    }

    @TestAccessible
    List<DigestRecord> collectFromNode() throws InterruptedException {
        // TODO: how to get logging to appear from remote call.
        log.debug("Collecting digests for {} artifacts", artifacts.size());

        List<DigestRecord> digests = new ArrayList<DigestRecord>(artifacts.size());

        for (ArtifactDTO artifact : artifacts) {
            String digest = null;

            // File information for the fingerprint.
            File file = ArtifactDTOHelper.getFile(artifact);
            // TODO: filter out nulls when generating the artifact list; i.e. at PerformFingerprinting or ArtifactRecorder
            String filename = null;
            if (file != null) {
                try {
                    digest = new FilePath(file).digest();
                    filename = file.getName();
                }
                // Record digest failures but don't let this stop processing of other artifact digests.
                catch (IOException e) {
                    log.error("Digest calculation failed: {}", file, e);
                }
            }
            
            // Always record that an attempt was made.  Unsuccessful attempts will have a null digest.
            // The caller should check for null digests and take appropriate action.
            digests.add(new DigestRecord(artifact, filename, digest));
        }

        return digests;
    }
}
