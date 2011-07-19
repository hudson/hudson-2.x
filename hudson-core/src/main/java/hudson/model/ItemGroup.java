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

package hudson.model;

import java.io.IOException;
import java.util.Collection;
import java.io.File;

/**
 * Represents a grouping inherent to a kind of {@link Item}s.
 *
 * @author Kohsuke Kawaguchi
 * @see ItemGroupMixIn
 */
public interface ItemGroup<T extends Item> extends PersistenceRoot, ModelObject {
    /**
     * Gets the full name of this {@link ItemGroup}.
     *
     * @see Item#getFullName() 
     */
    String getFullName();

    /**
     * @see Item#getFullDisplayName() 
     */
    String getFullDisplayName();

    /**
     * Gets all the items in this collection in a read-only view.
     */
    Collection<T> getItems();

    /**
     * Returns the path relative to the context root,
     * like "foo/bar/zot/". Note no leading slash but trailing slash.
     */
    String getUrl();

    /**
     * Gets the URL token that prefixes the URLs for child {@link Item}s.
     * Like "job", "item", etc.
     */
    String getUrlChildPrefix();

    /**
     * Gets the {@link Item} inside this group that has a given name.
     */
    T getItem(String name);

    /**
     * Assigns the {@link Item#getRootDir() root directory} for children.
     */
    File getRootDirFor(T child);

    /**
     * Internal method. Called by {@link Item}s when they are renamed by users.
     */
    void onRenamed(T item, String oldName, String newName) throws IOException;

    /**
     * Internal method. Called by {@link Item}s when they are deleted by users.
     */
    void onDeleted(T item) throws IOException;
}
