/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.AbstractHasData;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sonatype.matrix.gwt.common.TogglePanel;
import com.sonatype.matrix.maven.model.state.ArtifactDTO;
import com.sonatype.matrix.maven.model.state.BuildResultDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.BuildInfoResources;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.ModuleFormatter;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.ModuleInfoPickerPresenter;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.ModuleInfoPickerPresenter.ModuleInfoPickerView;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.ModuleInfoPresenter.ModuleInfoView;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.widget.ArtifactCoordinatesColumn;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Default {@link ModuleInfoView} implementation.
 *  
 * @author Jamie Whitehouse
 * @since 1.1
 */
@Singleton
public class ModuleInfoViewImpl
    extends Composite implements ModuleInfoView
{
    private static ModuleInfoDisplayImplUiBinder uiBinder = GWT.create( ModuleInfoDisplayImplUiBinder.class );

    @UiTemplate("ModuleInfoViewImpl.ui.xml")
    public interface ModuleInfoDisplayImplUiBinder
        extends UiBinder<Widget, ModuleInfoViewImpl>
    {
    }

    @UiField( provided = true )
    final ModuleInfoPickerView modulePicker;

    @UiField
    Image statusIcon;
    
    @UiField
    SpanElement buildSummary;

    @UiField
    SpanElement profileSummary;

    @UiField
    DivElement coordinatesText;

    @UiField
    AbstractHasData<ArtifactDTO> artifactInfo;
    
    @UiField
    CellTable<ArtifactDTO> producedArtifacts;
    
    @UiField
    Widget moduleDetailPanel;

    @UiField
    TogglePanel togglePanel;

    @Inject
    public ModuleInfoViewImpl( final ModuleInfoPickerPresenter pickerPresenter )
    {
        modulePicker = pickerPresenter.bind();
        
        initWidget( uiBinder.createAndBindUi( this ) );
        
        producedArtifacts.addColumn( new ArtifactCoordinatesColumn(), "Generated artifacts:" );
    }

    @Override
    public void setBuildStatus( BuildResultDTO result )
    {
        statusIcon.setResource( ModuleFormatter.resolveStatusIcon( BuildInfoResources.INSTANCE, result ) );
        statusIcon.setAltText( result.toString() );
    }

    @Override
    public void setBuildSummary( String summary )
    {
        buildSummary.setInnerText( summary );
    }
    
    @Override
    public void setCoordinates( String coordinates )
    {
        coordinatesText.setInnerText( coordinates );
    }

    @Override
    public void setProfileSummary( String summary )
    {
        profileSummary.setInnerText( summary );
    }
    
    @Override
    public void setProducedArtifacts( Collection<ArtifactDTO> data )
    {
        producedArtifacts.setVisible( data.size() != 0 );
        producedArtifacts.setRowData( new ArrayList<ArtifactDTO>(data) );
    }
    
    @Override
    public void setArtifactInfo( Collection<ArtifactDTO> data )
    {
        artifactInfo.setRowData( new ArrayList<ArtifactDTO>( data ) );
    }
    
    @Override
    public void showInfo()
    {
        togglePanel.showDetail();
    }
    
    @Override
    public void hideInfo()
    {
        togglePanel.showSummary();
    }
}
