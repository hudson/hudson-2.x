/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo;

import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.model.state.ArtifactDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.BuildInformationManagerImpl;

import java.util.Collection;

/**
 * Manages access to the build information.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
@ImplementedBy(BuildInformationManagerImpl.class)
public interface BuildInformationManager
{
    void refresh();

    Collection<ArtifactDTO> getConsumedArtifacts(String moduleId);

    Collection<ArtifactDTO> getProducedArtifacts(String moduleId);
}