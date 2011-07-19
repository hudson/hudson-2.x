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
*    Kohsuke Kawaguchi, Daniel Dyer, Tom Huybrechts, Yahoo!, Inc.
 *     
 *
 *******************************************************************************/ 

package hudson.tasks.test;

import java.util.Collection;

/**
 * Cumulated result of multiple tests.
 *
 * <p>
 * On top of {@link TestResult}, this class introduces a tree structure
 * of {@link TestResult}s.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class TabulatedResult extends TestResult {

    /**
     * Gets the child test result objects.
     *
     * @see TestObject#getParent()
     */
    public abstract Collection<? extends TestResult> getChildren();

    public abstract boolean hasChildren();

    public String getChildTitle() {
        return "";
    }
}
