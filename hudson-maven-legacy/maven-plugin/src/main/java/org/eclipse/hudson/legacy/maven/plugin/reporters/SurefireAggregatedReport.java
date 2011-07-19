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
*    Kohsuke Kawaguchi, id:cactusman, Yahoo!, Inc.
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin.reporters;

import hudson.model.Action;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.AggregatedTestResultAction;
import hudson.tasks.test.TestResultProjectAction;
import hudson.tasks.junit.CaseResult;

import java.util.List;
import java.util.Map;

import org.eclipse.hudson.legacy.maven.plugin.MavenAggregatedReport;
import org.eclipse.hudson.legacy.maven.plugin.MavenBuild;
import org.eclipse.hudson.legacy.maven.plugin.MavenModule;
import org.eclipse.hudson.legacy.maven.plugin.MavenModuleSet;
import org.eclipse.hudson.legacy.maven.plugin.MavenModuleSetBuild;

/**
 * {@link MavenAggregatedReport} for surefire report.
 * 
 * @author Kohsuke Kawaguchi
 */
public class SurefireAggregatedReport extends AggregatedTestResultAction implements MavenAggregatedReport {
    SurefireAggregatedReport(MavenModuleSetBuild owner) {
        super(owner);
    }

    public void update(Map<MavenModule, List<MavenBuild>> moduleBuilds, MavenBuild newBuild) {
        super.update(((MavenModuleSetBuild) owner).findModuleBuildActions(SurefireReport.class));
    }

    public Class<SurefireReport> getIndividualActionType() {
        return SurefireReport.class;
    }

    public Action getProjectAction(MavenModuleSet moduleSet) {
        return new TestResultProjectAction(moduleSet);
    }

    @Override
    protected String getChildName(AbstractTestResultAction tr) {
        return ((MavenModule)tr.owner.getProject()).getModuleName().toString();
    }

    @Override
    public MavenBuild resolveChild(Child child) {
        MavenModuleSet mms = (MavenModuleSet) owner.getProject();
        MavenModule m = mms.getModule(child.name);
        if(m!=null)
            return m.getBuildByNumber(child.build);
        return null;
    }

    public SurefireReport getChildReport(Child child) {
        MavenBuild b = resolveChild(child);
        if(b==null) return null;
        return b.getAction(SurefireReport.class);
    }
    
    /**
     * 
     */
    public String getTestResultPath(CaseResult it) {
        StringBuilder path = new StringBuilder("../");
        path.append(it.getOwner().getProject().getShortUrl());
        path.append(it.getOwner().getNumber());
        path.append("/");
        path.append(getUrlName());
        path.append("/");
        path.append(it.getRelativePathFrom(null));
        return path.toString();
    }
}
