/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.widgets;

import hudson.ExtensionPoint;
import hudson.model.Hudson;

/**
 * Box to be rendered in the side panel.
 *
 * <h2>Views</h2>
 * <ul>
 * <li><b>index.jelly</b> should display the widget. It should have:
 *   &lt;l:pane width="2" title="..."> ...body... &lt;/l:pane> structure.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.146
 * @see Hudson#getWidgets() 
 */
public abstract class Widget implements ExtensionPoint {
    /**
     * Gets the URL path name.
     *
     * <p>
     * For example, if this method returns "xyz", and if the parent object
     * (that this widget is associated with) is bound to /foo/bar/zot,
     * then this widget object will be exposed to /foo/bar/zot/xyz.
     *
     * <p>
     * This method is useful when the widget needs to expose additional URLs,
     * for example for serving AJAX requests.
     *
     * <p>
     * This method should return a string that's unique among other {@link Widget}s.
     * The default implementation returns the unqualified class name.
     */
    public String getUrlName() {
        return getClass().getSimpleName();
    }
}
