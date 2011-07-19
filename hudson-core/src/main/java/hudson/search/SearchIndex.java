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

import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public interface SearchIndex {
    void find(String token, List<SearchItem> result);

    /**
     *
     * This method returns the superset of {@link #find(String, List)}.
     */
    void suggest(String token, List<SearchItem> result);

    /**
     * Empty set.
     */
    static final SearchIndex EMPTY = new SearchIndex() {
        public void find(String token, List<SearchItem> result) {
            // no item to contribute
        }
        public void suggest(String token, List<SearchItem> result) {
            // nothing to suggest
        }
    };
}
