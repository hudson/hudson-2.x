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

package org.model.hudson.maven.eventspy.common;

import org.eclipse.hudson.maven.model.state.ArtifactDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;
import org.eclipse.hudson.maven.model.state.RuntimeEnvironmentDTO;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Provides the interface for invoking call backs from remote EventSpy to MavenBuilder.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public interface Callback
{
    /**
     * A reference to the <tt>.maven</tt> directory for the executing build.
     */
    File getMavenContextDirectory(); // FIXME: This does not belong here, more of a impl-side context detail

    /**
     * Returns true if the build should be aborted.
     */
    boolean isAborted();
    
    /**
     * Called to signal the end of processing.
     */
    void close();

    /**
     * Called after the spy has finished initializing and has established communication.
     */
    void setRuntimeEnvironment(RuntimeEnvironmentDTO env);

    /**
     * Prefer them to be sorted in topological order because we don't order them.
     */
    void setParticipatingProjects( List<MavenProjectDTO> projects );
    
    void updateParticipatingProject( MavenProjectDTO project );

    void setArtifacts(Collection<ArtifactDTO> artifacts);

    void addArtifacts(Collection<ArtifactDTO> artifacts);

    DocumentReference getSettingsDocument();

    DocumentReference getGlobalSettingsDocument();

    DocumentReference getToolChainsDocument();
}
