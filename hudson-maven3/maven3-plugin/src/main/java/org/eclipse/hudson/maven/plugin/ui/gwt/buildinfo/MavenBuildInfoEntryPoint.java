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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.gin.MavenBuildInfoInjector;
import org.fusesource.restygwt.client.Defaults;

/**
 * Display of build information.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class MavenBuildInfoEntryPoint
    implements EntryPoint
{
    private final MavenBuildInfoInjector injector = GWT.create(MavenBuildInfoInjector.class);

    public void onModuleLoad() {
        Log.setUncaughtExceptionHandler();
        injector.getScheduler().scheduleDeferred(new ScheduledCommand()
        {
            public void execute() {
                init();
            }
        });
    }

    private void init() {
        BuildInfoResources.INSTANCE.style().ensureInjected();

        String baseUri = getBaseRestURI();
        Log.debug("Base URI: " + baseUri);
        Defaults.setServiceRoot(baseUri);

        MavenBuildInfoController appController = injector.getAppController();
        appController.start(getPagePanel());
    }

    /**
     * The new GWT layout panels are being used which require size/resize 
     * notifications to operate correctly to fill the page. RootLayoutPanel 
     * should be the used when GWT controls the entire page.
     * 
     * RootPanel does not give these layout hints but it must be used to attach
     * to an existing element in the page (via div in this case).  When using 
     * RootPanel directly we need to set the height for the parent and the 
     * MainPanel as well as set it in the host page container div.
     * 
     * An easier approach is to add a LayoutPanel to the RootPanel and then use
     * that LayoutPanel as the container for the application.  In this way
     * resizing is accurate and we can attach to an existing page element; and
     * hopefully the rest of the app widgets will have to fuss with 100% sizing
     * provided they're used in the new layout containers that support accurate
     * resizing.
     * 
     * Refer to the GWT documentation for more information:
     * http://code.google.com/webtoolkit/doc/latest/DevGuideUiPanels.html#LayoutPanels
     * 
     * @return the panel to attach the application to 
     */
    private Panel getPagePanel() {
        // Use RootPanel in non-standards mode or when GWT doesn't control the
        // entire page. Tweaks are necessary to use this with widgets requiring
        // standards mode and the entire page.

        // Use RootLayoutPanel when GWT controls the entire page. Provides
        // better layout hints and resize events than RootPanel.
        // RootLayoutPanel.get().add( mainPanel );

        // appController.start( RootPanel.get( "maven3-buildInfoPanel" ), getProjectName(),
        // getBuildNumber() );
        LayoutPanel panel = new LayoutPanel();
        panel.setSize("100%", "100%");
        RootPanel.get("maven3-buildInfoPanel").add(panel);

        return panel;
    }

    private native String getBaseRestURI() /*-{
        return $wnd.baseRestURI;
    }-*/;
}
