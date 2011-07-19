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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.ModuleInfoPresenterImpl;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.ModuleInfoViewImpl;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;
import org.eclipse.hudson.maven.model.state.BuildResultDTO;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

import java.util.Collection;


/**
 * Presenter for {@link ModuleInfoView} widgets.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
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
