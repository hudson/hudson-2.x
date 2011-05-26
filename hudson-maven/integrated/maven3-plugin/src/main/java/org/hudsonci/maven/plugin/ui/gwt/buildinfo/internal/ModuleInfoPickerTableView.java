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

import com.google.gwt.user.cellview.client.AbstractHasData;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import org.hudsonci.gwt.common.MaximizedCellTable;
import org.hudsonci.maven.model.state.MavenProjectDTO;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.ModuleInfoPickerPresenter.ModuleInfoPickerView;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.widget.ModuleDurationColumn;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.widget.ModuleStatusIconColumn;

import static org.hudsonci.maven.model.MavenCoordinatesDTOHelper.RenderStyle;

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
