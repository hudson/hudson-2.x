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

package org.eclipse.hudson.maven.plugin.ui.gwt.configure;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import org.fusesource.restygwt.client.Defaults;

/**
 * MavenConfiguration entry-point.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class MavenConfigurationEntryPoint
    implements EntryPoint
{
    public static final String MAIN_PANEL_ID = "hudson-mavenConfigurationPanel";

    private final MavenConfigurationInjector injector = GWT.create(MavenConfigurationInjector.class);

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
        Log.debug("Loading");
        injector.getResources().style().ensureInjected();
        Defaults.setServiceRoot(getBaseRestURI());
        MavenConfigurationController controller = injector.getController();
        controller.start(getMainPanel());
    }

    private Panel getMainPanel() {
        LayoutPanel panel = new LayoutPanel();
        panel.setSize("100%", "100%");
        RootPanel.get(MAIN_PANEL_ID).add(panel);
        return panel;
    }

    private native String getBaseRestURI() /*-{
        return $wnd.baseRestURI;
    }-*/;
}
