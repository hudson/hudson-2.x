/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.eventspy.common;

import com.sonatype.matrix.maven.model.state.ArtifactDTO;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;
import com.sonatype.matrix.maven.model.state.RuntimeEnvironmentDTO;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Provides the interface for invoking call backs from remote EventSpy to MavenBuilder.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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
