/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal;

import com.google.gwt.user.client.Window;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.BuildCoordinates;

import javax.inject.Singleton;

/**
 * Build coordinates loaded from the page or query string.
 *
 * @author Jamie Whitehouse
 * @since 1.1
 */
@Singleton
public class GwtLoadedBuildCoordinates implements BuildCoordinates
{
    @Override
    public String getProjectName()
    {
        String projectNameFromPage = getProjectNameFromPage();
        return (null == projectNameFromPage ? Window.Location.getParameter( "project" ) : projectNameFromPage );
    }
    
    @Override
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
