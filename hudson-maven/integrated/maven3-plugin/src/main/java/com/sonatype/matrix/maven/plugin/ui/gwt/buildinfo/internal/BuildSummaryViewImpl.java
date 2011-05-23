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
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.sonatype.matrix.gwt.common.MaximizedCellTable;
import com.sonatype.matrix.maven.model.state.MavenProjectDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.BuildSummaryPresenter.BuildSummaryView;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.widget.ModuleDurationColumn;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.widget.ModuleStatusIconColumn;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Simple column view implementation of {@link BuildSummaryView}.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
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
    
    @Override
    public void setModuleData( final ModuleDataProvider mdp )
    {
        mdp.addDataDisplay( moduleSummary );
    }

    @Override
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
            @Override
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
        // MATRIX-424 indicates there's problems with this, disable for 2011 Q1 release.
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
