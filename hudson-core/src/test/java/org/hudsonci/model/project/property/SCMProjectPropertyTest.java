/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Nikita Levyankov
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
package org.hudsonci.model.project.property;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProjectMock;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.NullSCM;
import hudson.scm.PollingResult;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Contains test-cases for {@link SCMProjectProperty}.
 * <p/>
 * Date: 11/17/11
 *
 * @author Nikita Levyankov
 */
public class SCMProjectPropertyTest {

    private SCMProjectProperty property;
    private FreeStyleProjectMock project;

    @Before
    public void setUp() {
        project = new FreeStyleProjectMock("project");
        final String propertyKey = "propertyKey";
        property = new SCMProjectProperty(project);
        property.setKey(propertyKey);
    }

    /**
     * Verify constructor
     */
    @Test
    public void testConstructor() {
        try {
            new SCMProjectProperty(null);
            fail("Null should be handled by SCMProjectProperty constructor.");
        } catch (Exception e) {
            assertEquals(BaseProjectProperty.INVALID_JOB_EXCEPTION, e.getMessage());
        }
    }

    /**
     * Verify {@link SCMProjectProperty#getDefaultValue()} method.
     */
    @Test
    public void testGetDefaultValue() {
        assertEquals(new NullSCM(), property.getDefaultValue());
    }

    /**
     * Verify {@link SCMProjectProperty#returnOriginalValue()} method.
     */
    @Test
    public void testReturnOriginalValue() {
        //If property is marked as overridden or original value is not null and not equals to default value,
        //than original should be used.
        property.setOverridden(true);
        assertTrue(property.returnOriginalValue());
        property.setOriginalValue(new FakeSCM(), false);
        assertTrue(property.returnOriginalValue());

        //If property is not marked as overridden and original value is null or equals to default value - use cascading.
        property.setOriginalValue(property.getDefaultValue(), false);
        assertFalse(property.returnOriginalValue());
        property.setOriginalValue(null, false);
        assertFalse(property.returnOriginalValue());

    }

    private class FakeSCM extends SCM {
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
