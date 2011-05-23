/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * GWT bundle for stock Hudson resources.
 * 
 * See hudson.model.BallColor
 *
 * @author Jamie Whitehouse
 * @since 1.1
 */
public interface HudsonResources
    extends ClientBundle
{
    @Source("blue.png")
    ImageResource buildSuccessIcon();

    @Source("yellow.png")
    ImageResource buildWarnIcon();

    @Source("red.png")
    ImageResource buildFailureIcon();

    @Source("grey.png")
    ImageResource buildAbortIcon();

    @Source("grey.png")
    ImageResource buildDisabledIcon();
}
