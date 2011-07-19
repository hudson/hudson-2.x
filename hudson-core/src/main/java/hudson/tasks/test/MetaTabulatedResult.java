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
*    Kohsuke Kawaguchi, Yahoo!, Inc.
 *     
 *
 *******************************************************************************/ 

package hudson.tasks.test;


import java.util.Collection;

/**
 * The purpose of this class is to provide a good place for the
 * jelly to bind to.  
 * {@link TabulatedResult} whose immediate children
 * are other {@link TabulatedResult}s.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class MetaTabulatedResult extends TabulatedResult {

    /**
     * All failed tests.
     */
    public abstract Collection<? extends TestResult> getFailedTests();

}
