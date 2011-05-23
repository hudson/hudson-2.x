/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.ModuleDataProvider;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.ModuleInfoPickerPresenterImpl;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.ModuleInfoPickerTableView;

import javax.annotation.Nullable;

/**
 * Presenter for {@link ModuleInfoPickerView} widgets.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
@ImplementedBy(ModuleInfoPickerPresenterImpl.class)
public interface ModuleInfoPickerPresenter
{
    /**
     * Configures the view.
     * 
     * AKA Binds the presenter to the view.
     * 
     * @return the configured widget
     */
    ModuleInfoPickerView bind();

    /**
     * Indicates that the given module was selected.
     * 
     * Used to wire up the view selection events to the presenters logic.
     * 
     * @param pickedModule the module that was selected or null if one was deselected.
     */
    void moduleSelected(@Nullable MavenProjectDTO pickedModule);

    /**
     * Select the specified module. Used to communicate selection change from other components.
     * Callers should not invoke selectModule and moduleSelected, the latter will be called as
     * needed when this method is invoked.
     */
    void selectModule(MavenProjectDTO module);

    void refreshSelection();

    /**
     * Display of module selector to show a summary of the modules for selection to view details.
     */
    @ImplementedBy(ModuleInfoPickerTableView.class)
    public interface ModuleInfoPickerView
        extends IsWidget
    {
        void setSelectionModel(SelectionModel<? super MavenProjectDTO> selectionModel);

        void setData(ModuleDataProvider mdp);
    }
}