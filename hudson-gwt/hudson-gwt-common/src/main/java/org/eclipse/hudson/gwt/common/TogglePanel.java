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

package org.eclipse.hudson.gwt.common;

import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;

/**
 * Panel to toggle between a summary and a detail display.
 *
 * @author Jamie Whitehouse
 * @version 2.0.1
 */
public class TogglePanel
    extends ResizeComposite
{
    private final LayoutPanel container = new LayoutPanel();

    private final Label summary = new Label("Summary text.", true);
    private IsWidget detail;

    public TogglePanel()
    {
        container.add( summary );
        initWidget( container );
    }
    
    public TogglePanel( final String summaryText, final IsWidget detailWidget )
    {
        this();

        setDetail( detailWidget );
        setSummary( summaryText );
        
//        container.add( summary );
//        container.add( detail );
//        
//        initWidget( container );
    }
    
    @Override
    protected void onLoad()
    {
        showSummary();
    }
    
    public void setSummary( final String text )
    {
        summary.setText( text );
    }
    
    @UiChild( limit=1, tagname="detail" )
    public void setDetail( final IsWidget detailWidget )
    {
        if( detail != null )
        {
            container.remove( detail );
        }
        detail = detailWidget;
        container.add( detail );
    }
    
    public void showSummary()
    {
        summary.asWidget().setVisible( true );
        detail.asWidget().setVisible( false );
    }
    
    public void showDetail()
    {
        detail.asWidget().setVisible( true );
        summary.asWidget().setVisible( false );
    }
}
