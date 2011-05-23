/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal;

import com.google.gwt.user.cellview.client.AbstractHasData;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.sonatype.matrix.gwt.common.MaximizedCellTable;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.ModuleInfoPickerPresenter.ModuleInfoPickerView;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.widget.ModuleDurationColumn;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.widget.ModuleStatusIconColumn;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.sonatype.matrix.maven.model.MavenCoordinatesDTOHelper.RenderStyle;

/**
 * Table based view for {@link ModuleInfoPickerView}.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
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

    @Override
    public void setSelectionModel( final SelectionModel<? super MavenProjectDTO> selectionModel )
    {
        modulePicker.setSelectionModel( selectionModel );
    }
    
    @Override
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
            @Override
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
