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

package hudson.search;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * {@link SearchIndex} built on a {@link Map}.
 * 
 * @author Kohsuke Kawaguchi
 */
public abstract class CollectionSearchIndex<SMT extends SearchableModelObject> implements SearchIndex {
    /**
     * Gets a single item that exactly matches the given key.
     */
    protected abstract SearchItem get(String key);

    /**
     * Returns all items in the map.
     * The collection can include null items.
     */
    protected abstract Collection<SMT> all();

    public void find(String token, List<SearchItem> result) {
        SearchItem p = get(token);
        if(p!=null)
            result.add(p);
    }

    public void suggest(String token, List<SearchItem> result) {
        Collection<SMT> items = all();
        if(items==null)     return;
        for (SMT o : items) {
            if(o!=null && getName(o).contains(token))
                result.add(o);
        }
    }

    protected String getName(SMT o) {
        return o.getDisplayName();
    }
}
