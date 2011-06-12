/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.hudsonci.maven.model.state.MavenProjectDTO;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.ModuleInfoPickerPresenter;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.event.ModuleSelectedEvent;

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
