/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.AbstractHasData;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sonatype.matrix.maven.model.PropertiesDTO.Entry;
import com.sonatype.matrix.maven.model.state.ArtifactDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.BuildInfoResources;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.BuildSummaryPresenter.BuildSummaryView;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.MainPanelPresenter;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.MainPanelPresenter.MainPanelView;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.ModuleInfoPresenter.ModuleInfoView;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.widget.PropertiesTable;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link MainPanelView} implementation.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
@Singleton
public class MainPanelViewImpl
    extends Composite implements MainPanelView
{
    private static MainPanelUiBinder uiBinder = GWT.create( MainPanelUiBinder.class );

    @UiTemplate("MainPanelViewImpl.ui.xml")
    interface MainPanelUiBinder
        extends UiBinder<Widget, MainPanelViewImpl>
    {
    }
    
    @UiField
    Panel statePickerPanel;
    
    @UiField
    ListBox statePicker;
    
    @UiField
    Image refresh;

    @UiField
    TabLayoutPanel tabPanel;
    
    @UiField( provided = true )
    final BuildSummaryView summaryInfo;

    @UiField
    PropertiesTable versionInfo;

    @UiField
    PropertiesTable userInfo;

    @UiField
    PropertiesTable systemInfo;

    @UiField
    PropertiesTable environmentInfo;

    @UiField(provided=true)
    final ModuleInfoView moduleInfo;
    
    @UiField
    AbstractHasData<ArtifactDTO> artifactInfo;

    @UiField
    AbstractPager artifactPager;

    private MainPanelPresenter presenter;

    @Inject
    public MainPanelViewImpl( final ModuleDataProvider mdp, final ArtifactDataProvider adp, final BuildSummaryView buildSummaryView, final ModuleInfoView moduleInfoView )
    {
        summaryInfo = checkNotNull(buildSummaryView);
        moduleInfo = moduleInfoView;
        
        initWidget( uiBinder.createAndBindUi( this ) );

        initRefresh();
        statePickerPanel.setVisible( false );

        initArtifactView( adp );
    }
    
    @Override
    public void setPresenter( MainPanelPresenter presenter )
    {
        this.presenter = presenter;
    }
    
    @Override
    protected void onLoad()
    {
        statePicker.addChangeHandler( new ChangeHandler()
        {
            @Override
            public void onChange( ChangeEvent event )
            {
                presenter.buildStateSelected( statePicker.getSelectedIndex() );
            }
        });
    }

    @Override
    public void setFirstShownWidget( IsWidget widget )
    {
        tabPanel.selectTab( widget );
    }
    
    @Override
    public void setVersionData( List<Entry> data)
    {
        versionInfo.setRowData( data );
    }
    
    @Override
    public void setUserData( List<Entry> data)
    {
        userInfo.setRowData( data );
    }
    
    @Override
    public void setSystemData( List<Entry> data)
    {
        systemInfo.setRowData( data );
    }

    @Override
    public void setEnvironmentData( List<Entry> data)
    {
        environmentInfo.setRowData( data );
    }

    @Override
    public void setStateSelectionNames( final List<String> stateNames )
    {
        checkNotNull(stateNames);
        
        statePicker.clear();
        for ( String name : stateNames )
        {
            statePicker.addItem( name );
        }
    }

    @Override
    public void showStatePicker(final int selectedIndex)
    {
        statePicker.setSelectedIndex(selectedIndex);
        statePickerPanel.setVisible( true );
    }

    @Override
    public void selectModuleInfo()
    {
        tabPanel.selectTab( moduleInfo );
    }
    
    private void initRefresh()
    {
        refresh.setResource( BuildInfoResources.INSTANCE.refresh() );
        refresh.setAltText( "Refresh information." );
        refresh.addClickHandler( new ClickHandler()
        {
            @Override
            public void onClick( ClickEvent event )
            {
                presenter.refresh();
            }
        });
    }

    private void initArtifactView( final ArtifactDataProvider adp )
    {
        adp.addDataDisplay( artifactInfo );
        artifactPager.setDisplay( artifactInfo );
    }

}
