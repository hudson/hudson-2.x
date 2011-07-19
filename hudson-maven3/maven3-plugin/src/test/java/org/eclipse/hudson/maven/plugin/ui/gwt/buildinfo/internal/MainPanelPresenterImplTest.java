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

import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.MainPanelPresenter;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.MainPanelPresenter.MainPanelView;
import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.MainPanelPresenterImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of {@link org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.MainPanelPresenterImpl}
 *
 * @author Jamie Whitehouse
 */
@RunWith( MockitoJUnitRunner.class )
public class MainPanelPresenterImplTest
{
    @Mock
    private MainPanelView view;

    @BeforeClass
    public static void disarmGwtCreate()
    {
        GWTMockUtilities.disarm();
    }

    @AfterClass
    public static void armGwtCreate()
    {
        GWTMockUtilities.restore();
    }

    @Test
    public void panelIsAddedToContainer()
    {
        HasWidgets container = mock( HasWidgets.class );
        Widget viewWidget = mock( Widget.class );
        when( view.asWidget() ).thenReturn( viewWidget );

        MainPanelPresenter mainPanelPresenter = new MainPanelPresenterImpl( view, null, null, null );
        mainPanelPresenter.bind( container );

        verify( container ).add( viewWidget );
    }

    @SuppressWarnings( "unused" )
    @Test
    public void firstShownWidgetIsSetWhenInitialized()
    {
        Widget firstShownWidget = mock( Widget.class );

        new MainPanelPresenterImpl( view, firstShownWidget, null, null );

        verify( view ).setFirstShownWidget( firstShownWidget );
    }
}
