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

import hudson.matrix.Combination;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.Action;

/**
 * {@link Action} that aggregates all the test results from {@link MatrixRun}s.
 *
 * <p>
 * This object is attached to {@link MatrixBuild}.
 *
 * @author Kohsuke Kawaguchi
 */
public class MatrixTestResult extends AggregatedTestResultAction {
    public MatrixTestResult(MatrixBuild owner) {
        super(owner);
    }

    /**
     * Use the configuration name.
     */
    @Override
    protected String getChildName(AbstractTestResultAction tr) {
        return tr.owner.getProject().getName();
    }

    @Override
    public AbstractBuild<?,?> resolveChild(Child child) {
        MatrixBuild b = (MatrixBuild)owner;
        return b.getRun(Combination.fromString(child.name));
    }

    @Override
    public String getTestResultPath(TestResult it) {
        // Prepend Configuration path
        return it.getOwner().getParent().getShortUrl() + super.getTestResultPath(it);
    }
}
