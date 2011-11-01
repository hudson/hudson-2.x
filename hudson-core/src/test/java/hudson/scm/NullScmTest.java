/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Oracle Corporation, Anton Kozak
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.scm;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Test to verify {@link NullSCM}
 */
@RunWith(Parameterized.class)
public class NullScmTest {
    private SCM scm1;
    private SCM scm2;
    private boolean expectedResult;

    public NullScmTest(SCM scm1, SCM scm2, boolean expectedResult) {
        this.scm1 = scm1;
        this.scm2 = scm2;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters
    public static Collection generateData() {
        return Arrays.asList(new Object[][]{
            {new NullSCM(), new NullSCM(), true},
            {new NullSCM(), null, false},
            {new NullSCM(), new FakeSCM(), false}
        });

    }

    @Test
    public void testEquals() {
        assertEquals(expectedResult, scm1.equals(scm2));
    }

    @Test
    public void testHashCode() {
        assertEquals(expectedResult, scm1.hashCode() == (scm2 == null? 0: scm2).hashCode());
    }

    private static class FakeSCM extends SCM {
        @Override
        public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher,
                                                       TaskListener listener)
            throws IOException, InterruptedException {
            return null;
        }

        @Override
        protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> project, Launcher launcher,
                                                          FilePath workspace, TaskListener listener,
                                                          SCMRevisionState baseline)
            throws IOException, InterruptedException {
            return null;
        }

        @Override
        public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace,
                                BuildListener listener,
                                File changelogFile) throws IOException, InterruptedException {
            return false;
        }

        @Override
        public ChangeLogParser createChangeLogParser() {
            return null;
        }
    }
}
