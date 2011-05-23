/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.configure;

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
 * @since 1.1
 */
public class MavenConfigurationEntryPoint
    implements EntryPoint
{
    public static final String MAIN_PANEL_ID = "sonatype-mavenConfigurationPanel";

    private final MavenConfigurationInjector injector = GWT.create(MavenConfigurationInjector.class);

    @Override
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
