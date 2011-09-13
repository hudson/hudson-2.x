
/*******************************************************************************
 *
 * Copyright (c) 2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *    Nikita Levyankov
 *
 *******************************************************************************/
package hudson.model;

import hudson.Functions;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.matrix.TextAxis;
import hudson.tasks.Ant;
import hudson.tasks.ArtifactArchiver;
import hudson.tasks.BatchFile;
import hudson.tasks.Fingerprinter;
import hudson.tasks.Maven;
import hudson.tasks.Shell;
import java.io.IOException;
import java.util.List;
import org.junit.Ignore;
import org.jvnet.hudson.test.Email;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.SingleFileSCM;
import org.jvnet.hudson.test.UnstableBuilder;

/**
 * Testcases for matrix project. We moved from MatrixProjectTest.groovy
 * <p/>
 * Date: 6/17/11
 *
 * @author Nikita Levyankov
 */
public class Matrix2ProjectTest extends HudsonTestCase {
    /**
     * Tests that axes are available as build variables in the Ant builds.
     */
    public void testBuildAxisInAnt() throws Exception {
        MatrixProject p = createMatrixProject();
        Ant.AntInstallation ant = configureDefaultAnt();
        p.getBuildersList().add(new Ant("-Dprop=${db} test",ant.getName(),null,null,null));

        // we need a dummy build script that echos back our property
        p.setScm(new SingleFileSCM("build.xml","<project default=\"test\"><target name=\"test\"><echo>assertion ${prop}=${db}</echo></target></project>"));

        MatrixBuild build = p.scheduleBuild2(0, new Cause.UserCause()).get();
        List<MatrixRun> runs = build.getRuns();
        assertEquals(4,runs.size());
        for (MatrixRun run : runs) {
            assertBuildStatus(Result.SUCCESS, run);
            String expectedDb = run.getParent().getCombination().get("db");
            assertLogContains("assertion "+expectedDb+"="+expectedDb, run);
        }
    }

    /**
     * Tests that axes are available as build variables in the Maven builds.
     */
    //TODO fix this test
    public void ignore_testBuildAxisInMaven() throws Exception {
        MatrixProject p = createMatrixProject();
        Maven.MavenInstallation maven = configureDefaultMaven();
        p.getBuildersList().add(new Maven("-Dprop=${db} validate",maven.getName()));

        // we need a dummy build script that echos back our property
        p.setScm(new SingleFileSCM("pom.xml", getClass().getResource("echo-property.pom")));

        MatrixBuild build = p.scheduleBuild2(0).get();
        List<MatrixRun> runs = build.getRuns();
        assertEquals(4,runs.size());
        for (MatrixRun run : runs) {
            assertBuildStatus(Result.SUCCESS, run);
            String expectedDb = run.getParent().getCombination().get("db");
            System.out.println(run.getLog());
            assertLogContains("assertion "+expectedDb+"="+expectedDb, run);
            // also make sure that the variables are expanded at the command line level.
            assertFalse(run.getLog().contains("-Dprop=${db}"));
        }
    }

    /**
     * Test that configuration filters work
     */
    public void testConfigurationFilter() throws Exception {
        MatrixProject p = createMatrixProject();
        p.setCombinationFilter("db==\"mysql\"");
        MatrixBuild build = p.scheduleBuild2(0).get();
        assertEquals(2, build.getRuns().size());
    }

    /**
     * Test that touch stone builds  work
     */
    public void testTouchStone() throws Exception {
        MatrixProject p = createMatrixProject();
        p.setTouchStoneCombinationFilter("db==\"mysql\"");
        p.setTouchStoneResultCondition(Result.SUCCESS);
        MatrixBuild build = p.scheduleBuild2(0).get();
        assertEquals(4, build.getRuns().size());

        p.getBuildersList().add(new UnstableBuilder());
        build = p.scheduleBuild2(0).get();
        assertEquals(2, build.getRuns().size());
    }

    @Override
    protected MatrixProject createMatrixProject() throws IOException {
        MatrixProject p = super.createMatrixProject();

        // set up 2x2 matrix
        AxisList axes = new AxisList();
        axes.add(new TextAxis("db","mysql","oracle"));
        axes.add(new TextAxis("direction","north","south"));
        p.setAxes(axes);

        return p;
    }

    /**
     * Fingerprinter failed to work on the matrix project.
     */
    @Email("http://www.nabble.com/1.286-version-and-fingerprints-option-broken-.-td22236618.html")
    public void testFingerprinting() throws Exception {
        MatrixProject p = createMatrixProject();
        if (Functions.isWindows())
           p.getBuildersList().add(new BatchFile("echo \"\" > p"));
        else
           p.getBuildersList().add(new Shell("touch p"));

        p.getPublishersList().add(new ArtifactArchiver("p",null,false));
        p.getPublishersList().add(new Fingerprinter("",true));
        buildAndAssertSuccess(p);
    }
}
