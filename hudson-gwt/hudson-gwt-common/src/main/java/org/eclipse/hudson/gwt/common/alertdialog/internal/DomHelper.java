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

package org.eclipse.hudson.gwt.common.alertdialog.internal;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

/**
 * Common functions for manipulating {@link DOM} elements.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class DomHelper
{
    /**
     * Make sure the element is always on top.
     */
    public static void onTop(final Element element) {
        // Use camel case style names that GWT prefers instead of the standard name.
        // Not "z-index".  GWT code has assertions for this which are enabled
        // in dev mode.
        DOM.setIntStyleAttribute(element, "zIndex", Integer.MAX_VALUE);
    }
}
