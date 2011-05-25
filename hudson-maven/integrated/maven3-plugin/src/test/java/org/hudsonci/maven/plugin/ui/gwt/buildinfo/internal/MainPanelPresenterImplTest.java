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

import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.MainPanelPresenter;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.MainPanelPresenter.MainPanelView;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.MainPanelPresenterImpl;
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
 * Test of {@link org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.MainPanelPresenterImpl}
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
