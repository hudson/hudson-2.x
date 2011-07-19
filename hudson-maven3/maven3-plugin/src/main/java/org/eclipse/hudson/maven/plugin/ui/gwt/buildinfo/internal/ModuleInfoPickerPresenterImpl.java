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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.ModuleInfoPickerPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.event.ModuleSelectedEvent;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

import javax.inject.Inject;
import javax.inject.Singleton;


import java.util.NoSuchElementException;

/**
 * Default implementation of {@link ModuleInfoPickerPresenter}. 
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Singleton
public class ModuleInfoPickerPresenterImpl implements ModuleInfoPickerPresenter
{
    private final ModuleInfoPickerView view;
    private final EventBus eventBus;
    private final ModuleDataProvider mdp;

    private final SingleSelectionModel<MavenProjectDTO> selectionModel;
    private String currentSelectionId;

    @Inject
    public ModuleInfoPickerPresenterImpl( final ModuleInfoPickerView view, final EventBus eventBus, final ModuleDataProvider mdp )
    {
        this.view = view;
        this.eventBus = eventBus;
        this.mdp = mdp;
        this.selectionModel = createSelectionModel();
    }

    public ModuleInfoPickerView bind()
    {
        view.setSelectionModel( selectionModel );
        view.setData( mdp );
        return view;
    }

    private SingleSelectionModel<MavenProjectDTO> createSelectionModel()
    {
        final SingleSelectionModel<MavenProjectDTO> moduleSelectionModel = new SingleSelectionModel<MavenProjectDTO>(ModuleDataProvider.KEY_PROVIDER);
        moduleSelectionModel.addSelectionChangeHandler( new SelectionChangeEvent.Handler()
        {
            public void onSelectionChange( SelectionChangeEvent event )
            {
                moduleSelected( moduleSelectionModel.getSelectedObject() );
            }
        });
        
        return moduleSelectionModel;
    }
    
    public void selectModule( final MavenProjectDTO module )
    {
        Log.debug("Selecting module: " + module);
        selectionModel.setSelected( module, true );
    }
    
    public void moduleSelected( final MavenProjectDTO pickedModule )
    {
        if(pickedModule != null) {
            currentSelectionId = pickedModule.getId();
            Log.debug("Module selected; updated currentSelectionId: " + currentSelectionId);
        } else {
            currentSelectionId = null;
            Log.debug("Module deselected.");
        }
        eventBus.fireEvent( new ModuleSelectedEvent( pickedModule ) );
    }

    /**
     * Make up for the fact that changing the underlying data does not change
     * the selection state when the keys are the same.
     * 
     * If the selected module is still selected let listeners know.
     */
    public void refreshSelection() {
        if(currentSelectionId != null)
        {
            // If the current selection is in the data list then refresh the selection
            try {
                moduleSelected(mdp.find(currentSelectionId));
                Log.debug("Refreshing selection to previously selected moduleId: "  + currentSelectionId);
            }
            // Otherwise remove the selection since the module isn't relevant for the current view.
            catch (NoSuchElementException e) {
                MavenProjectDTO selected = selectionModel.getSelectedObject();
                Log.debug("Deselecting previously selected module: " + selected.getId());
                // This will trigger the selectionChangeHandler, causing the relevant presenter methods to be called
                // using a null object for the events module.
                selectionModel.setSelected(selected, false);
            }
        }
    }
}
