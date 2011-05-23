/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.model.PropertiesDTO.Entry;
import com.sonatype.matrix.maven.model.state.BuildStateDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.MainPanelPresenterImpl;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.MainPanelViewImpl;

import java.util.List;

/**
 * Presenter for {@link MainPanelView} widgets.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
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