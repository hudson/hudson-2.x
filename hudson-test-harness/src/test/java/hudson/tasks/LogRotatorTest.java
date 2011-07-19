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

package hudson.tasks;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Cause.LegacyCodeCause;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.ArtifactArchiverTest.CreateArtifact;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * Verifies that the last successful and stable builds of a job will be kept if requested.
 */
public class LogRotatorTest extends HudsonTestCase {

    public void testSuccessVsFailure() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        project.setLogRotator(new LogRotator(-1, 2, -1, -1));
        assertEquals(Result.SUCCESS, build(project)); // #1
        project.getBuildersList().replaceBy(Collections.singleton(new FailureBuilder()));
        assertEquals(Result.FAILURE, build(project)); // #2
        assertEquals(Result.FAILURE, build(project)); // #3
        assertEquals(1, numberOf(project.getLastSuccessfulBuild()));
        project.getBuildersList().replaceBy(Collections.<Builder>emptySet());
        assertEquals(Result.SUCCESS, build(project)); // #4
        assertEquals(4, numberOf(project.getLastSuccessfulBuild()));
        assertEquals(null, project.getBuildByNumber(1));
        assertEquals(null, project.getBuildByNumber(2));
        assertEquals(3, numberOf(project.getLastFailedBuild()));
    }

    @Bug(2417)
    public void testStableVsUnstable() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        project.setLogRotator(new LogRotator(-1, 2, -1, -1));
        assertEquals(Result.SUCCESS, build(project)); // #1
        project.getPublishersList().replaceBy(Collections.singleton(new TestsFail()));
        assertEquals(Result.UNSTABLE, build(project)); // #2
        assertEquals(Result.UNSTABLE, build(project)); // #3
        assertEquals(1, numberOf(project.getLastStableBuild()));
        project.getPublishersList().replaceBy(Collections.<Publisher>emptySet());
        assertEquals(Result.SUCCESS, build(project)); // #4
        assertEquals(null, project.getBuildByNumber(1));
        assertEquals(null, project.getBuildByNumber(2));
    }

    @Bug(834)
    public void testArtifactDelete() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        project.setLogRotator(new LogRotator(-1, 6, -1, 2));
        project.getPublishersList().replaceBy(Collections.singleton(new ArtifactArchiver("f", "", true)));
        assertEquals("(no artifacts)", Result.FAILURE, build(project)); // #1
        assertFalse(project.getBuildByNumber(1).getHasArtifacts());
        project.getBuildersList().replaceBy(Collections.singleton(new CreateArtifact()));
        assertEquals(Result.SUCCESS, build(project)); // #2
        assertTrue(project.getBuildByNumber(2).getHasArtifacts());
        project.getBuildersList().replaceBy(Arrays.asList(new CreateArtifact(), new FailureBuilder()));
        assertEquals(Result.FAILURE, build(project)); // #3
        assertTrue(project.getBuildByNumber(2).getHasArtifacts());
        assertTrue(project.getBuildByNumber(3).getHasArtifacts());
        assertEquals(Result.FAILURE, build(project)); // #4
        assertTrue(project.getBuildByNumber(2).getHasArtifacts());
        assertTrue(project.getBuildByNumber(3).getHasArtifacts());
        assertTrue(project.getBuildByNumber(4).getHasArtifacts());
        assertEquals(Result.FAILURE, build(project)); // #5
        assertTrue(project.getBuildByNumber(2).getHasArtifacts());
        assertFalse("no better than #4", project.getBuildByNumber(3).getHasArtifacts());
        assertTrue(project.getBuildByNumber(4).getHasArtifacts());
        assertTrue(project.getBuildByNumber(5).getHasArtifacts());
        project.getBuildersList().replaceBy(Collections.singleton(new CreateArtifact()));
        assertEquals(Result.SUCCESS, build(project)); // #6
        assertFalse("#2 is still lastSuccessful until #6 is complete", project.getBuildByNumber(2).getHasArtifacts());
        assertFalse(project.getBuildByNumber(3).getHasArtifacts());
        assertFalse(project.getBuildByNumber(4).getHasArtifacts());
        assertTrue(project.getBuildByNumber(5).getHasArtifacts());
        assertTrue(project.getBuildByNumber(6).getHasArtifacts());
        assertEquals(Result.SUCCESS, build(project)); // #7
        assertEquals(null, project.getBuildByNumber(1));
        assertNotNull(project.getBuildByNumber(2));
        assertFalse("lastSuccessful was #6 for ArtifactArchiver", project.getBuildByNumber(2).getHasArtifacts());
        assertFalse(project.getBuildByNumber(3).getHasArtifacts());
        assertFalse(project.getBuildByNumber(4).getHasArtifacts());
        assertFalse(project.getBuildByNumber(5).getHasArtifacts());
        assertTrue(project.getBuildByNumber(6).getHasArtifacts());
        assertTrue(project.getBuildByNumber(7).getHasArtifacts());
        assertEquals(Result.SUCCESS, build(project)); // #8
        assertEquals(null, project.getBuildByNumber(2));
        assertNotNull(project.getBuildByNumber(3));
        assertFalse(project.getBuildByNumber(3).getHasArtifacts());
        assertFalse(project.getBuildByNumber(4).getHasArtifacts());
        assertFalse(project.getBuildByNumber(5).getHasArtifacts());
        assertFalse(project.getBuildByNumber(6).getHasArtifacts());
        assertTrue(project.getBuildByNumber(7).getHasArtifacts());
        assertTrue(project.getBuildByNumber(8).getHasArtifacts());
    }


    static Result build(FreeStyleProject project) throws Exception {
        return project.scheduleBuild2(0, new LegacyCodeCause()).get(10, TimeUnit.SECONDS).getResult();
    }

    private static int numberOf(Run<?,?> run) {
        return run != null ? run.getNumber() : -1;
    }

    static class TestsFail extends Publisher {
        public @Override boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) {
            build.setResult(Result.UNSTABLE);
            return true;
        }

        public BuildStepMonitor getRequiredMonitorService() {
            return BuildStepMonitor.NONE;
        }

        public Descriptor<Publisher> getDescriptor() {
            return new Descriptor<Publisher>(TestsFail.class) {
                public String getDisplayName() {
                    return "TestsFail";
                }
            };
        }
    }

}
