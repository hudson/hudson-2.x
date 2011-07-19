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
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.ImplementedBy;

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.ModuleDataProvider;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.ModuleInfoPickerPresenterImpl;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.ModuleInfoPickerTableView;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

import org.sonatype.inject.Nullable;


/**
 * Presenter for {@link ModuleInfoPickerView} widgets.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
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
