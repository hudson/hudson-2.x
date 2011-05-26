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
import org.hudsonci.maven.model.PropertiesDTO.Entry;
import org.hudsonci.maven.model.state.ArtifactDTO;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.BuildInfoResources;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.MainPanelPresenter;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.BuildSummaryPresenter.BuildSummaryView;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.MainPanelPresenter.MainPanelView;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.ModuleInfoPresenter.ModuleInfoView;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.widget.PropertiesTable;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link MainPanelView} implementation.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
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
    
    public void setPresenter( MainPanelPresenter presenter )
    {
        this.presenter = presenter;
    }
    
    protected void onLoad()
    {
        statePicker.addChangeHandler( new ChangeHandler()
        {
            public void onChange( ChangeEvent event )
            {
                presenter.buildStateSelected( statePicker.getSelectedIndex() );
            }
        });
    }

    public void setFirstShownWidget( IsWidget widget )
    {
        tabPanel.selectTab( widget );
    }
    
    public void setVersionData( List<Entry> data)
    {
        versionInfo.setRowData( data );
    }
    
    public void setUserData( List<Entry> data)
    {
        userInfo.setRowData( data );
    }
    
    public void setSystemData( List<Entry> data)
    {
        systemInfo.setRowData( data );
    }

    public void setEnvironmentData( List<Entry> data)
    {
        environmentInfo.setRowData( data );
    }

    public void setStateSelectionNames( final List<String> stateNames )
    {
        checkNotNull(stateNames);
        
        statePicker.clear();
        for ( String name : stateNames )
        {
            statePicker.addItem( name );
        }
    }

    public void showStatePicker(final int selectedIndex)
    {
        statePicker.setSelectedIndex(selectedIndex);
        statePickerPanel.setVisible( true );
    }

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
