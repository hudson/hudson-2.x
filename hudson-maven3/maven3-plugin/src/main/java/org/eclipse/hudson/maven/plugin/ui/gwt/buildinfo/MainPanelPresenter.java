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

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.MainPanelPresenterImpl;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.MainPanelViewImpl;
import org.eclipse.hudson.maven.model.PropertiesDTO.Entry;
import org.eclipse.hudson.maven.model.state.BuildStateDTO;

import java.util.List;


/**
 * Presenter for {@link MainPanelView} widgets.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@ImplementedBy(MainPanelPresenterImpl.class)
public interface MainPanelPresenter
{
    void bind(HasWidgets container);

    void setBuildStates(List<BuildStateDTO> buildStates);

    void buildStateSelected(int selectedIndex);

    void selectModuleInfo();

    void refresh();

    /**
     * Main UI panel.
     */
    @ImplementedBy(MainPanelViewImpl.class)
    public interface MainPanelView
        extends IsWidget
    {
        void setFirstShownWidget(IsWidget widget);

        void setVersionData(List<Entry> data);

        void setEnvironmentData(List<Entry> data);

        void setSystemData(List<Entry> data);

        void setUserData(List<Entry> data);

        void setStateSelectionNames(List<String> stateNames);

        void showStatePicker(int selectedIndex);

        void setPresenter(MainPanelPresenter presenter);

        void selectModuleInfo();
    }
}
