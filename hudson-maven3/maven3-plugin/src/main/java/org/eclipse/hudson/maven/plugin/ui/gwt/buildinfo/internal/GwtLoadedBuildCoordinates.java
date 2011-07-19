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

import com.google.gwt.user.client.Window;

import javax.inject.Singleton;

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.BuildCoordinates;

/**
 * Build coordinates loaded from the page or query string.
 *
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Singleton
public class GwtLoadedBuildCoordinates implements BuildCoordinates
{
    public String getProjectName()
    {
        String projectNameFromPage = getProjectNameFromPage();
        return (null == projectNameFromPage ? Window.Location.getParameter( "project" ) : projectNameFromPage );
    }
    
    public int getBuildnumber()
    {
        String buildNumberFromPage = getBuildNumberFromPage();
        return Integer.parseInt(null == buildNumberFromPage ? Window.Location.getParameter( "build" ) : buildNumberFromPage );
    }

    private native String getProjectNameFromPage() /*-{
        return $wnd.projectName;
    }-*/;

    private native String getBuildNumberFromPage() /*-{
        return $wnd.buildNumber;
    }-*/;
}
