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
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.RowCountChangeEvent;

import org.eclipse.hudson.gwt.common.MaximizedCellTable;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.BuildSummaryPresenter.BuildSummaryView;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.widget.ModuleDurationColumn;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.widget.ModuleStatusIconColumn;
import org.eclipse.hudson.maven.model.state.MavenProjectDTO;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * Simple column view implementation of {@link BuildSummaryView}.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Singleton
public class BuildSummaryViewImpl
    extends ResizeComposite implements BuildSummaryView
{
    private final AbstractHasData<MavenProjectDTO> moduleSummary;
    private final Label builderSummary;
    
    @Inject
    public BuildSummaryViewImpl()
    {
        builderSummary = new Label();
        moduleSummary = initModuleSummary( createModuleTable() );

        VerticalPanel layout;
        layout = new VerticalPanel();
        layout.add( builderSummary );
        layout.add( moduleSummary );

        ScrollPanel container = new ScrollPanel();
        container.add( layout );
        initWidget( container );
    }
    
    public void setModuleData( final ModuleDataProvider mdp )
    {
        mdp.addDataDisplay( moduleSummary );
    }

    public void setBuildSummaryText( String summaryText )
    {
        builderSummary.setText( summaryText );
    }

    /**
     * @return the view passed in for method chaining.
     */
    private <T> AbstractHasData<T> initModuleSummary( final AbstractHasData<T> moduleView )
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

    private AbstractHasData<MavenProjectDTO> createModuleTable( )
    {
        CellTable<MavenProjectDTO> table = new MaximizedCellTable<MavenProjectDTO>();
        
        // Header to span all columns.
        TextHeader header = new TextHeader( "Modules" );

        table.addColumn( new ModuleStatusIconColumn(), header );

        table.addColumn(getModuleNameColumn(), header);
        
        table.addColumn( new ModuleDurationColumn(), header );
        
        return table;
    }

    private TextColumn<MavenProjectDTO> getModuleNameColumn() {
//        return new Column<MavenProjectDTO, Hyperlink>( new HyperlinkCell() )
//        {
//            @Override
//            public Hyperlink getValue( MavenProjectDTO module )
//            {
//                return new Hyperlink( module.getName(), "module-" + module.getId() );
//            }
//        };
        
        return new TextColumn<MavenProjectDTO>()
        {
            @Override
            public String getValue(MavenProjectDTO module) {
                return module.getName();
            }
        };
    }
}
