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

package hudson.scm.browsers;

/**
 * Builds up a query string.
 *
 * @author Kohsuke Kawaguchi
*/
public final class QueryBuilder {
    private final StringBuilder buf = new StringBuilder();

    public QueryBuilder(String s) {
        add(s);
    }

    public QueryBuilder add(String s) {
        if(s==null)     return this; // nothing to add
        if(buf.length()==0) buf.append('?');
        else                buf.append('&');
        buf.append(s);
        return this;
    }

    @Override
    public String toString() {
        return buf.toString();
    }
}
