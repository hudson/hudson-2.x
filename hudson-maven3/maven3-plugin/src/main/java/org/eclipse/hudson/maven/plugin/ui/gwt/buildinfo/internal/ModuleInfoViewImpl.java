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

import org.eclipse.hudson.gwt.common.TogglePanel;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.BuildInfoResources;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.ModuleFormatter;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.ModuleInfoPickerPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.ModuleInfoPickerPresenter.ModuleInfoPickerView;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.ModuleInfoPresenter.ModuleInfoView;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.widget.ArtifactCoordinatesColumn;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;
import org.eclipse.hudson.maven.model.state.BuildResultDTO;

import javax.inject.Inject;
import javax.inject.Singleton;


import java.util.ArrayList;
import java.util.Collection;

/**
 * Default {@link ModuleInfoView} implementation.
 *  
 * @author Jamie Whitehouse
 * @since 2.1.0
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

    public void setBuildStatus( BuildResultDTO result )
    {
        statusIcon.setResource( ModuleFormatter.resolveStatusIcon( BuildInfoResources.INSTANCE, result ) );
        statusIcon.setAltText( result.toString() );
    }

    public void setBuildSummary( String summary )
    {
        buildSummary.setInnerText( summary );
    }
    
    public void setCoordinates( String coordinates )
    {
        coordinatesText.setInnerText( coordinates );
    }

    public void setProfileSummary( String summary )
    {
        profileSummary.setInnerText( summary );
    }

    public void setProducedArtifacts( Collection<ArtifactDTO> data )
    {
        producedArtifacts.setVisible( data.size() != 0 );
        producedArtifacts.setRowData( new ArrayList<ArtifactDTO>(data) );
    }
    
    public void setArtifactInfo( Collection<ArtifactDTO> data )
    {
        artifactInfo.setRowData( new ArrayList<ArtifactDTO>( data ) );
    }
    
    public void showInfo()
    {
        togglePanel.showDetail();
    }
    
    public void hideInfo()
    {
        togglePanel.showSummary();
    }
}
