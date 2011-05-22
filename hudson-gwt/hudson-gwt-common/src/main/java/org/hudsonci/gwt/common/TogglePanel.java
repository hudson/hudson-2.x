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

package org.hudsonci.gwt.common;

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
