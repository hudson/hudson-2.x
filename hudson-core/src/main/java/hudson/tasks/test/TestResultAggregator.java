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

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.BuildListener;

import java.io.IOException;

/**
 * Aggregates {@link AbstractTestResultAction}s of {@link MatrixRun}s
 * into {@link MatrixBuild}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class TestResultAggregator extends MatrixAggregator {
    private MatrixTestResult result;

    public TestResultAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
        super(build, launcher, listener);
    }

    @Override
    public boolean startBuild() throws InterruptedException, IOException {
        result = new MatrixTestResult(build);
        build.addAction(result);
        return true;
    }

    @Override
    public boolean endRun(MatrixRun run) throws InterruptedException, IOException {
        AbstractTestResultAction atr = run.getAction(AbstractTestResultAction.class);
        if(atr!=null)   result.add(atr);
        return true;
    }
}
