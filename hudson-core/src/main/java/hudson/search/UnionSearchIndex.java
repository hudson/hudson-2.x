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
 * Union of two sets.
 *
 * @author Kohsuke Kawaguchi
 */
public class UnionSearchIndex implements SearchIndex {
    public static SearchIndex combine(SearchIndex... sets) {
        SearchIndex p=EMPTY;
        for (SearchIndex q : sets) {
            // allow some of the inputs to be null,
            // and also recognize EMPTY
            if (q != null && q != EMPTY) {
                if (p == EMPTY)
                    p = q;
                else
                    p = new UnionSearchIndex(p,q);
            }
        }
        return p;
    }

    private final SearchIndex lhs,rhs;

    public UnionSearchIndex(SearchIndex lhs, SearchIndex rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void find(String token, List<SearchItem> result) {
        lhs.find(token,result);
        rhs.find(token,result);
    }

    public void suggest(String token, List<SearchItem> result) {
        lhs.suggest(token,result);
        rhs.suggest(token,result);
    }
}
