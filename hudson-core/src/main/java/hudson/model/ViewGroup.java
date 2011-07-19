/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Kohsuke Kawaguchi,   Tom Huybrechts, Alan Harder
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.security.AccessControlled;
import hudson.views.ViewsTabBar;

import java.io.IOException;
import java.util.Collection;

/**
 * Container of {@link View}s.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.269
 */
public interface ViewGroup extends Saveable, ModelObject, AccessControlled {
    /**
     * Determine whether a view may be deleted.
     * @since 1.365
     */
    boolean canDelete(View view);

    /**
     * Deletes a view in this group.
     */
    void deleteView(View view) throws IOException;

    /**
     * Gets all the views in this group.
     *
     * @return
     *      can be empty but never null.
     */
    Collection<View> getViews();

    /**
     * Gets a view of the given name.
     *
     * This also creates the URL binding for views (in the form of ".../view/FOOBAR/...")
     */
    View getView(String name);

    /**
     * Returns the path of this group, relative to the context root,
     * like "foo/bar/zot/". Note no leading slash but trailing slash.
     */
    String getUrl();

    /**
     * {@link View} calls this method when it's renamed.
     * This method is intended to work as a notification to the {@link ViewGroup}
     * (so that it can adjust its internal data structure, for example.)
     *
     * <p>
     * It is the caller's responsibility to ensure that the new name is a
     * {@linkplain Hudson#checkGoodName(String) legal view name}.
     */
    void onViewRenamed(View view, String oldName, String newName);

    /**
     * Gets the TabBar for the views.
     *
     * TabBar for views can be provided by extension. Only one TabBar can be active
     * at a given time (Selectable by user in the global Configuration page).
     * Default TabBar is provided by Hudson Platform.
     * @since 1.381
     */
    ViewsTabBar getViewsTabBar();
}
