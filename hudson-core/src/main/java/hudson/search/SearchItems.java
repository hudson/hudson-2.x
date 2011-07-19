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

/**
 * @author Kohsuke Kawaguchi
 */
public class SearchItems {
    public static SearchItem create(String searchName, String url) {
        return create(searchName,url, SearchIndex.EMPTY);
    }

    public static SearchItem create(final String searchName, final String url, final SearchIndex children) {
        return new SearchItem() {
            public String getSearchName() {
                return searchName;
            }

            public String getSearchUrl() {
                return url;
            }

            public SearchIndex getSearchIndex() {
                return children;
            }
        };
    }

    public static SearchItem create(final String searchName, final String url, final SearchableModelObject searchable) {
        return new SearchItem() {
            public String getSearchName() {
                return searchName;
            }

            public String getSearchUrl() {
                return url;
            }

            public SearchIndex getSearchIndex() {
                return searchable.getSearchIndex();
            }
        };
    }
}
