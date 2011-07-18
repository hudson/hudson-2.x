/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.maven;

import hudson.model.Result;
import hudson.tasks.Maven.MavenInstallation;

import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.ExtractResourceSCM;
import org.jvnet.hudson.test.HudsonTestCase;
import org.eclipse.hudson.legacy.maven.plugin.MavenModuleSet;

/**
 * @author Olivier Lamy
 */
public class MavenBuildSurefireFailedTest extends HudsonTestCase {

    @Bug(8415)
    public void testMaven2Unstable() throws Exception {
        configureDefaultMaven();
        MavenModuleSet m = createMavenProject();
        m.setGoals( "test" );
        m.setScm(new ExtractResourceSCM(getClass().getResource("maven-multimodule-unit-failure.zip")));
        assertBuildStatus(Result.UNSTABLE, m.scheduleBuild2(0).get());
    }
    
    @Bug(8415)
    public void testMaven2Failed() throws Exception {
        configureDefaultMaven();
        MavenModuleSet m = createMavenProject();
        m.setGoals( "test -Dmaven.test.failure.ignore=false" );
        m.setScm(new ExtractResourceSCM(getClass().getResource("maven-multimodule-unit-failure.zip")));
        assertBuildStatus(Result.FAILURE, m.scheduleBuild2(0).get());
    }   
    
    @Bug(8415)
    //TODO: Revisit this test case
    public void ignore_testMaven3Unstable() throws Exception {
        MavenModuleSet m = createMavenProject();
        m.setMaven( configureMaven3().getName() );
        m.setGoals( "test" );
        m.setScm(new ExtractResourceSCM(getClass().getResource("maven-multimodule-unit-failure.zip")));
        assertBuildStatus(Result.UNSTABLE, m.scheduleBuild2(0).get());
    }
    
    @Bug(8415)
    public void testMaven3Failed() throws Exception {
        MavenModuleSet m = createMavenProject();
        m.setMaven( configureMaven3().getName() );
        m.setGoals( "test -Dmaven.test.failure.ignore=false" );
        m.setScm(new ExtractResourceSCM(getClass().getResource("maven-multimodule-unit-failure.zip")));
        assertBuildStatus(Result.FAILURE, m.scheduleBuild2(0).get());
    }    
    
    
}
