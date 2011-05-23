/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * {@link ClientBundle} resource for the build information view.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
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
        @ClassName("sonatype-moduleResultFAILURE")
        String sonatypeModuleResultFAILURE();

        @ClassName("sonatype-moduleResultSUCCESS")
        String sonatypeModuleResultSUCCESS();

        @ClassName("sonatype-moduleResultSKIPPED")
        String sonatypeModuleResultSKIPPED();
    }
}
