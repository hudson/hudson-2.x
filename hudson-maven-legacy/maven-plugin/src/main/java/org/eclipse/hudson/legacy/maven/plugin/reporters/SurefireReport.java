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

package org.eclipse.hudson.legacy.maven.plugin.reporters;

import hudson.model.BuildListener;
import hudson.model.Action;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;

import java.util.List;
import java.util.Map;

import org.eclipse.hudson.legacy.maven.plugin.AggregatableAction;
import org.eclipse.hudson.legacy.maven.plugin.MavenAggregatedReport;
import org.eclipse.hudson.legacy.maven.plugin.MavenBuild;
import org.eclipse.hudson.legacy.maven.plugin.MavenModule;
import org.eclipse.hudson.legacy.maven.plugin.MavenModuleSetBuild;

/**
 * {@link Action} that displays surefire test result.
 * @author Kohsuke Kawaguchi
 */
public class SurefireReport extends TestResultAction implements AggregatableAction {
    SurefireReport(MavenBuild build, TestResult result, BuildListener listener) {
        super(build, result, listener);
    }

    public MavenAggregatedReport createAggregatedAction(MavenModuleSetBuild build, Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        return new SurefireAggregatedReport(build);
    }
}
