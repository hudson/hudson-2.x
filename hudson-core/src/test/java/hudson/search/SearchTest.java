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

import junit.framework.TestCase;

import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class SearchTest extends TestCase {
    public void test1() {
        SearchIndex si = new SearchIndexBuilder()
            .add("abc-def-ghi","abc def ghi")
            .add(SearchItems.create("abc","abc",
                new SearchIndexBuilder()
                    .add("def-ghi","def ghixxx")
                    .make()))
            .make();

        SuggestedItem x = Search.find(si, "abc def ghi");
        assertNotNull(x);
        assertEquals(x.getUrl(),"/abc-def-ghi");

        List<SuggestedItem> l = Search.suggest(si, "abc def ghi");
        assertEquals(2,l.size());
        assertEquals("/abc-def-ghi",l.get(0).getUrl());
        assertEquals("/abc/def-ghi",l.get(1).getUrl());
    }
}
