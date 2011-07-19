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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * GWT bundle for stock Hudson resources.
 * 
 * See hudson.model.BallColor
 *
 * @author Jamie Whitehouse
 * @since 2.1.0
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
