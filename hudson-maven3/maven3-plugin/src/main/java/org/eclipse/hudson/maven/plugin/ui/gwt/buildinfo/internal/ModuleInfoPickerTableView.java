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

import com.google.gwt.user.cellview.client.AbstractHasData;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;

import org.eclipse.hudson.gwt.common.MaximizedCellTable;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.ModuleInfoPickerPresenter.ModuleInfoPickerView;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.widget.ModuleDurationColumn;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.widget.ModuleStatusIconColumn;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

import javax.inject.Inject;
import javax.inject.Singleton;


import static org.eclipse.hudson.maven.model.MavenCoordinatesDTOHelper.RenderStyle;

/**
 * Table based view for {@link ModuleInfoPickerView}.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Singleton
public class ModuleInfoPickerTableView
    extends ResizeComposite implements ModuleInfoPickerView
{
    private final AbstractHasData<MavenProjectDTO> modulePicker;
    
    @Inject
    public ModuleInfoPickerTableView()
    {
        modulePicker = init( createTable() );
        initWidget( new ScrollPanel(modulePicker) );
    }

    public void setSelectionModel( final SelectionModel<? super MavenProjectDTO> selectionModel )
    {
        modulePicker.setSelectionModel( selectionModel );
    }
    
    public void setData( final ModuleDataProvider mdp )
    {
        mdp.addDataDisplay( modulePicker );        
    }

    /**
     * @return the view passed in for method chaining.
     */
    private <T> AbstractHasData<T> init( final AbstractHasData<T> moduleView )
    {
        moduleView.addRowCountChangeHandler( new RowCountChangeEvent.Handler()
        {
            public void onRowCountChange( RowCountChangeEvent event )
            {
                moduleView.setPageSize( event.getNewRowCount() );
            }
        });
        
        return moduleView;
    }
    
    private CellTable<MavenProjectDTO> createTable()
    {
        CellTable<MavenProjectDTO> table = new MaximizedCellTable<MavenProjectDTO>(ModuleDataProvider.KEY_PROVIDER);
        
        table.addColumn( new ModuleStatusIconColumn(), "Status" );
        
        table.addColumn( new TextColumn<MavenProjectDTO>()
        {
            @Override
            public String getValue( MavenProjectDTO module )
            {
                return module.getName();
            }
        }, "Name" );
        
        table.addColumn( new TextColumn<MavenProjectDTO>()
        {
            @Override
            public String getValue( MavenProjectDTO module )
            {
                return module.getCoordinates().toString(RenderStyle.GAV);
            }
        }, "GAV" );
        
        table.addColumn( new ModuleDurationColumn(), "Duration" );
      
        return table;
    }
}
