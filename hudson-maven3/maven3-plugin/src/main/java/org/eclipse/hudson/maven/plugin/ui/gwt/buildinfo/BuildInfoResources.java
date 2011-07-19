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

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * {@link ClientBundle} resource for the build information view.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public interface BuildInfoResources
    extends HudsonResources
{
    public static final BuildInfoResources INSTANCE = GWT.create(BuildInfoResources.class);

    @Source("MavenBuildInfoStyle.css")
    Style style();

    @Source("time.png")
    ImageResource activityScheduled();

    @Source("time_go.png")
    ImageResource activityExecuting();

    @Source("arrow_refresh.png")
    ImageResource refresh();

    public interface Style
        extends CssResource
    {
        @ClassName("maven3-moduleResultFAILURE")
        String maven3ModuleResultFAILURE();

        @ClassName("maven3-moduleResultSUCCESS")
        String maven3ModuleResultSUCCESS();

        @ClassName("maven3-moduleResultSKIPPED")
        String maven3ModuleResultSKIPPED();
    }
}
