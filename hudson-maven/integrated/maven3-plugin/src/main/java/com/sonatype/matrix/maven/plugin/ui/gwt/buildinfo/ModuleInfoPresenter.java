/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.model.state.ArtifactDTO;
import com.sonatype.matrix.maven.model.state.BuildResultDTO;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.ModuleInfoPresenterImpl;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.ModuleInfoViewImpl;

import java.util.Collection;

/**
 * Presenter for {@link ModuleInfoView} widgets.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
@ImplementedBy(ModuleInfoPresenterImpl.class)
public interface ModuleInfoPresenter
{
    /**
     * Set the module to display information for.
     */
    void setModule(MavenProjectDTO module);

    void clear();

    /**
     * Display of {@link MavenProjectDTO} information.
     */
    @ImplementedBy(ModuleInfoViewImpl.class)
    public interface ModuleInfoView
        extends IsWidget
    {
        void setBuildStatus(BuildResultDTO result);

        void setBuildSummary(String summary);

        void setCoordinates(String coordinates);

        void setProfileSummary(String summary);

        void setProducedArtifacts(Collection<ArtifactDTO> data);

        void setArtifactInfo(Collection<ArtifactDTO> data);

        void hideInfo();

        void showInfo();
    }
}
