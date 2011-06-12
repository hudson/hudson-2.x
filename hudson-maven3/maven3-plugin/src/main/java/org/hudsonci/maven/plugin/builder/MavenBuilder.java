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

package org.hudsonci.maven.plugin.builder;

import org.hudsonci.maven.model.config.BuildConfigurationDTO;
import org.hudsonci.maven.model.config.DocumentDTO;
import org.hudsonci.maven.model.state.BuildStateDTO;
import org.hudsonci.service.NodeService;
import org.hudsonci.service.SecurityService;
import org.hudsonci.utils.plugin.ui.JellyAccessible;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import org.hudsonci.maven.plugin.builder.internal.PerformBuild;
import org.hudsonci.maven.plugin.documents.DocumentNotFoundException;
import org.hudsonci.maven.plugin.install.MavenInstallation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.security.ACL;
import hudson.tasks.Builder;

import javax.inject.Inject;

import java.io.IOException;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Hudson Maven {@link Builder}.
 * 
 * WARNING: for {@link Build}s (which includes multi-configuration builds) the 
 * same {@link Builder} instance is used for all executions. Hence do not store state 
 * in the builder if it's specific to an execution. Instead use the {@link BuildStateRecord}
 * or {@link BuildStateDTO} created in {@link #perform(AbstractBuild, Launcher, BuildListener)} 
 * by passing it to the necessary objects. In the future we may use a ThreadLocal 
 * or some other context during the life of a build execution.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@XStreamAlias("maven-builder")
public class MavenBuilder
    extends Builder
{
    private static final Logger log = LoggerFactory.getLogger(MavenBuilder.class);

    private final BuildConfigurationDTO config;

    @XStreamOmitField
    private SecurityService security;

    @XStreamOmitField
    private NodeService nodes;

    public MavenBuilder(final BuildConfigurationDTO config) {
        this.config = checkNotNull(config);
    }

    @Inject
    public void setSecurity(final SecurityService security) {
        this.security = checkNotNull(security);
    }

    private SecurityService getSecurity() {
        checkState(security != null);
        return security;
    }

    @Inject
    public void setNodes(final NodeService nodes) {
        this.nodes = checkNotNull(nodes);
    }

    public NodeService getNodes() {
        checkState(nodes != null);
        return nodes;
    }

    @Override
    public MavenBuilderDescriptor getDescriptor() {
        return (MavenBuilderDescriptor) super.getDescriptor();
    }

    @JellyAccessible
    public BuildConfigurationDTO getConfig() {
        return config;
    }

    public MavenInstallation getMavenInstallation() {
        String installationId = getConfig().getInstallationId();

        if (installationId != null) {
            for (MavenInstallation installation : getDescriptor().getInstallations()) {
                if (installationId.equals(installation.getName())) {
                    return installation;
                }
            }
        }

        return null;
    }

    public DocumentDTO getDocument(final String id) {
        // HACK: Handle special case from shitty Jelly configuration crap, "NONE" == null ATM.
        // HACK: ... When configured via REST and we use null for none then drop this check (keeping null check only)
        if (id == null || "NONE".equals(id)) {
            return null;
        }

        log.debug("Getting document for ID: {}", id);
        DocumentDTO document = null;
        try {
            // Need to run as SYSTEM when fetching the documents to be used
            document = getSecurity().callAs2(ACL.SYSTEM, new Callable<DocumentDTO>()
            {
                public DocumentDTO call() {
                    return getDescriptor().getDocuments().getDocument(id, false);
                }
            });
            log.debug("Document: {}", document);
        }
        catch (DocumentNotFoundException e) {
            log.warn("Ignoring missing document for ID: {}", id);
        }

        return document;
    }

    @Override
    public boolean perform(final AbstractBuild<?,?> build, final Launcher launcher, final BuildListener listener)
        throws InterruptedException, IOException
    {
        BuildStateDTO state = attachBuildState( build ).getState();
        attachBuildAction( build );

        // Attach the build configuration to the state
        state.setBuildConfiguration(getConfig());

        // Perform the build
        boolean result = new PerformBuild(this, state, build, launcher, listener).execute();

        // TODO: free up the BuildStateDTO hard-ref once the build has saved to disk, so that the xref soft-reference
        // (when configured) will allow it to be reclaimed when running low on memory.
        // Refer to change history for the removed BuildStateClearer.
        
        return result;
    }

    /**
     * Attach the build state record and prime the build state instance.
     * @return the record attached to the build 
     */
    private BuildStateRecord attachBuildState( final AbstractBuild<?, ?> build )
    {
        BuildStateRecord record = new BuildStateRecord(build);
        build.addAction(record);
        return record;
    }

    /**
     * Attach the build action if its not already attached
     */
    private void attachBuildAction( final AbstractBuild<?, ?> build )
    {
        if (build.getAction(MavenBuildAction.class) == null) {
            build.addAction(new MavenBuildAction(build));
        }
    }
}
