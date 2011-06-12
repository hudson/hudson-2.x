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

package org.hudsonci.maven.plugin.ui.gwt.configure;

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
    public static final String MAIN_PANEL_ID = "sonatype-mavenConfigurationPanel";

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
