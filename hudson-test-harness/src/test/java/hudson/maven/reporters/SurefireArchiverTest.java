/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *       
 *
 *******************************************************************************/ 

package hudson.maven.reporters;

import org.eclipse.hudson.legacy.maven.plugin.MavenBuild;
import org.eclipse.hudson.legacy.maven.plugin.MavenProjectActionBuilder;
import hudson.model.Result;
import org.jvnet.hudson.test.ExtractResourceSCM;
import org.jvnet.hudson.test.HudsonTestCase;
import org.eclipse.hudson.legacy.maven.plugin.MavenModuleSet;
import org.eclipse.hudson.legacy.maven.plugin.MavenModuleSetBuild;
import org.eclipse.hudson.legacy.maven.plugin.reporters.SurefireArchiver;

/**
 * @author Kohsuke Kawaguchi
 */
public class SurefireArchiverTest extends HudsonTestCase {

    public void testSerialization() throws Exception {
        configureDefaultMaven();
        MavenModuleSet m = createMavenProject();
        m.setScm(new ExtractResourceSCM(getClass().getResource("../maven-surefire-unstable.zip")));
        m.setGoals("install");

        MavenModuleSetBuild b = m.scheduleBuild2(0).get();
        assertBuildStatus(Result.UNSTABLE, b);


        MavenBuild mb = b.getModuleLastBuilds().values().iterator().next();
        boolean foundFactory = false, foundSurefire = false;
        for (MavenProjectActionBuilder x : mb.getProjectActionBuilders()) {
            if (x instanceof SurefireArchiver.FactoryImpl) { 
                foundFactory = true;
            }
            if (x instanceof SurefireArchiver) {
                foundSurefire = true;
            }
        }

        assertTrue(foundFactory);
        assertFalse(foundSurefire);
    }
}
