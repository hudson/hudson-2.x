/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Nikita Levyankov, Anton Kozak
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
package hudson.util;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.model.FreeStyleProjectMock;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.ParametersDefinitionProperty;
import hudson.security.AuthorizationMatrixProperty;
import hudson.tasks.JavadocArchiver;
import hudson.tasks.Publisher;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Test cases for cascading utils.
 * <p/>
 * Date: 10/25/11
 *
 * @author Nikita Levyankov
 */
@RunWith(PowerMockRunner.class)
public class CascadingUtilTest {

    @Test
    @PrepareForTest(Hudson.class)
    public void testUnlinkProjectFromCascadingParents() throws Exception {
        //Prepare data
        FreeStyleProject project1 = new FreeStyleProjectMock("project1");
        FreeStyleProjectMock child1 = new FreeStyleProjectMock("child1");
        String cascadingName = "newCascadingProject";
        FreeStyleProjectMock child = new FreeStyleProjectMock(cascadingName);
        child1.setCascadingProject(project1);
        CascadingUtil.linkCascadingProjectsToChild(child1, cascadingName);

        List<Job> jobs = new ArrayList<Job>();
        jobs.add(project1);
        jobs.add(child1);
        jobs.add(child);

        Hudson hudson = createMock(Hudson.class);
        mockStatic(Hudson.class);
        expect(hudson.getAllItems(Job.class)).andReturn(jobs).anyTimes();
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replay(Hudson.class, hudson);

        //Can't unlink from null project
        assertFalse(CascadingUtil.unlinkProjectFromCascadingParents(null, cascadingName));
        //Can't unlink null cascading name
        assertFalse(CascadingUtil.unlinkProjectFromCascadingParents(project1, null));

        //Verify whether cascadingName is present in parent and child
        assertTrue(project1.getCascadingChildrenNames().contains(cascadingName));
        assertTrue(child1.getCascadingChildrenNames().contains(cascadingName));
        boolean result = CascadingUtil.unlinkProjectFromCascadingParents(child1, cascadingName);
        assertTrue(result);
        //Name should disappear from hierarchy.
        assertFalse(project1.getCascadingChildrenNames().contains(cascadingName));
        assertFalse(child1.getCascadingChildrenNames().contains(cascadingName));

        CascadingUtil.linkCascadingProjectsToChild(project1, cascadingName);
        assertTrue(project1.getCascadingChildrenNames().contains(cascadingName));
        result = CascadingUtil.unlinkProjectFromCascadingParents(child1, cascadingName);
        assertTrue(result);
        assertFalse(project1.getCascadingChildrenNames().contains(cascadingName));

    }

    @Test
    @PrepareForTest(Hudson.class)
    public void testUnlinkProjectFromCascadingParents2() throws Exception {
        FreeStyleProject project1 = new FreeStyleProjectMock("p1");
        FreeStyleProjectMock project2 = new FreeStyleProjectMock("p2");
        FreeStyleProjectMock project3 = new FreeStyleProjectMock("p3");
        project2.setCascadingProject(project1);
        CascadingUtil.linkCascadingProjectsToChild(project1, "p2");
        project3.setCascadingProject(project2);
        CascadingUtil.linkCascadingProjectsToChild(project2, "p3");

        List<Job> jobs = new ArrayList<Job>();
        jobs.add(project1);
        jobs.add(project2);
        jobs.add(project3);

        Hudson hudson = createMock(Hudson.class);
        mockStatic(Hudson.class);
        expect(hudson.getAllItems(Job.class)).andReturn(jobs);
        expect(Hudson.getInstance()).andReturn(hudson);
        replay(Hudson.class, hudson);

        CascadingUtil.unlinkProjectFromCascadingParents(project1, "p2");
        //Project3 should disappear from project1's children.
        assertTrue(project1.getCascadingChildrenNames().isEmpty());
    }

    @Test
    public void testLinkCascadingProjectsToChild() throws Exception {
        FreeStyleProject project1 = new FreeStyleProjectMock("project1");
        FreeStyleProjectMock child1 = new FreeStyleProjectMock("child1");
        child1.setCascadingProject(project1);
        String cascadingName = "newCascadingProject";
        CascadingUtil.linkCascadingProjectsToChild(null, cascadingName);
        assertFalse(project1.getCascadingChildrenNames().contains(cascadingName));
        assertFalse(child1.getCascadingChildrenNames().contains(cascadingName));

        CascadingUtil.linkCascadingProjectsToChild(project1, cascadingName);
        assertTrue(project1.getCascadingChildrenNames().contains(cascadingName));

        project1 = new FreeStyleProjectMock("project1");
        child1 = new FreeStyleProjectMock("child1");
        child1.setCascadingProject(project1);
        CascadingUtil.linkCascadingProjectsToChild(child1, cascadingName);
        //Name should be included to all cascading parents up-hierarchy.
        assertTrue(project1.getCascadingChildrenNames().contains(cascadingName));
        assertTrue(child1.getCascadingChildrenNames().contains(cascadingName));
    }


    @Test
    public void testRenameCascadingChildLinks() throws Exception {
        String oldName = "oldCascadingProject";
        String newName = "newCascadingProject";
        FreeStyleProject project1 = new FreeStyleProjectMock("project1");
        FreeStyleProjectMock project2 = new FreeStyleProjectMock("project2");
        FreeStyleProjectMock project3 = new FreeStyleProjectMock(oldName);
        project2.setCascadingProject(project1);
        CascadingUtil.linkCascadingProjectsToChild(project1, "project2");
        project3.setCascadingProject(project2);
        CascadingUtil.linkCascadingProjectsToChild(project2, oldName);
        assertTrue(project2.getCascadingChildrenNames().contains(oldName));
        assertTrue(project1.getCascadingChildrenNames().contains(oldName));

        CascadingUtil.renameCascadingChildLinks(project2, oldName, newName);

        assertTrue(project2.getCascadingChildrenNames().contains(newName));
        assertFalse(project2.getCascadingChildrenNames().contains(oldName));
        assertTrue(project1.getCascadingChildrenNames().contains(newName));
        assertFalse(project1.getCascadingChildrenNames().contains(oldName));
    }

    @Test
    public void testRenameCascadingChildLinksNullProject() throws Exception {
        CascadingUtil.renameCascadingChildLinks(null, "name", "newName");
    }

    @Test
    @PrepareForTest(Hudson.class)
    public void testRenameCascadingParentLinks() throws Exception {
        String oldName = "oldCascadingProject";
        String newName = "newCascadingProject";
        List<Job> jobs = new ArrayList<Job>();
        FreeStyleProject project1 = new FreeStyleProjectMock(oldName);
        FreeStyleProjectMock project2 = new FreeStyleProjectMock("child");
        project2.setCascadingProject(project1);
        jobs.add(project1);
        jobs.add(project2);
        mockStatic(Hudson.class);
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getAllItems(Job.class)).andReturn(jobs);
        expect(Hudson.getInstance()).andReturn(hudson);
        replay(Hudson.class, hudson);
        CascadingUtil.renameCascadingParentLinks(oldName, newName);
        verify(Hudson.class, hudson);
        assertEquals(newName, project2.getCascadingProjectName());
    }

    @Test
    public void testRenameCascadingParentLinksNullNames() {
        CascadingUtil.renameCascadingParentLinks(null, null);
    }

    @Test
    public void testRenameCascadingParentLinksEmptyNames() {
        CascadingUtil.renameCascadingParentLinks("", "");
    }

    @Test
    public void testRenameCascadingParentLinksNullName1() {
        CascadingUtil.renameCascadingParentLinks(null, "name");
    }

    @Test
    public void testRenameCascadingParentLinksNullName2() {
        CascadingUtil.renameCascadingParentLinks("name", null);
    }

    @Test
    @PrepareForTest(Hudson.class)
    public void testGetAllItems() throws Exception {
        FreeStyleProject project1 = new FreeStyleProjectMock("p1");
        FreeStyleProjectMock project2 = new FreeStyleProjectMock("p2");
        FreeStyleProjectMock project3 = new FreeStyleProjectMock("p3");
        FreeStyleProjectMock project4 = new FreeStyleProjectMock("p4");
        List<Job> jobs = new ArrayList<Job>();
        jobs.add(project1);
        jobs.add(project2);
        jobs.add(project3);
        jobs.add(project4);

        Hudson hudson = createMock(Hudson.class);
        mockStatic(Hudson.class);
        expect(hudson.getAllItems(Job.class)).andReturn(jobs).anyTimes();
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replay(Hudson.class, hudson);

        project2.setCascadingProject(project1);
        CascadingUtil.linkCascadingProjectsToChild(project1, "p2");
        project3.setCascadingProject(project1);
        CascadingUtil.linkCascadingProjectsToChild(project1, "p3");
        project4.setCascadingProject(project3);
        CascadingUtil.linkCascadingProjectsToChild(project3, "p4");

        List<Job> jobs1 = CascadingUtil.getCascadingParents(Job.class, project1);
        List<Job> jobs2 = CascadingUtil.getCascadingParents(Job.class, project2);
        List<Job> jobs3 = CascadingUtil.getCascadingParents(Job.class, project3);
        List<Job> jobs4 = CascadingUtil.getCascadingParents(Job.class, project4);
        verify(Hudson.class, hudson);

        assertEquals(0, jobs1.size());
        assertFalse(jobs1.contains(project1));
        assertFalse(jobs1.contains(project2));
        assertFalse(jobs1.contains(project3));
        assertFalse(jobs1.contains(project4));

        assertEquals(3, jobs2.size());
        assertTrue(jobs2.contains(project1));
        assertFalse(jobs2.contains(project2));
        assertTrue(jobs2.contains(project3));
        assertTrue(jobs2.contains(project4));

        assertEquals(2, jobs3.size());
        assertTrue(jobs3.contains(project1));
        assertTrue(jobs3.contains(project2));
        assertFalse(jobs3.contains(project3));
        assertFalse(jobs3.contains(project4));

        assertEquals(3, jobs4.size());
        assertTrue(jobs4.contains(project1));
        assertTrue(jobs4.contains(project2));
        assertTrue(jobs4.contains(project3));
        assertFalse(jobs4.contains(project4));
    }

    @Test
    @PrepareForTest(Hudson.class)
    public void testHasCyclicCascadingLink() throws Exception {
        FreeStyleProject project1 = new FreeStyleProjectMock("p1");
        FreeStyleProjectMock project2 = new FreeStyleProjectMock("p2");
        FreeStyleProjectMock project3 = new FreeStyleProjectMock("p3");
        FreeStyleProjectMock project4 = new FreeStyleProjectMock("p4");
        List<Job> jobs = new ArrayList<Job>();
        jobs.add(project1);
        jobs.add(project2);
        jobs.add(project3);
        jobs.add(project4);

        Hudson hudson = createMock(Hudson.class);
        mockStatic(Hudson.class);
        expect(hudson.getAllItems(Job.class)).andReturn(jobs).anyTimes();
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replay(Hudson.class, hudson);

        project2.setCascadingProject(project1);
        CascadingUtil.linkCascadingProjectsToChild(project1, "p2");
        project3.setCascadingProject(project1);
        CascadingUtil.linkCascadingProjectsToChild(project1, "p3");
        project4.setCascadingProject(project3);
        CascadingUtil.linkCascadingProjectsToChild(project3, "p4");

        verifyCyclicCascadingLink(true, project2, project1);
        verifyCyclicCascadingLink(true, project3, project1);
        verifyCyclicCascadingLink(true, project4, project3);
        verifyCyclicCascadingLink(true, project4, project1);
        verifyCyclicCascadingLink(false, project3, project2);
        verifyCyclicCascadingLink(false, project1, project2);
        verifyCyclicCascadingLink(false, project2, project3);
        verifyCyclicCascadingLink(false, null, project1);
        verifyCyclicCascadingLink(false, null, project4);

        verify(Hudson.class, hudson);
    }

    private void verifyCyclicCascadingLink(boolean expectedResult, Job candidateJob, Job currentJob) {
        assertEquals(expectedResult,
            CascadingUtil.hasCyclicCascadingLink(candidateJob, currentJob.getCascadingChildrenNames()));
    }

    @Test
    @PrepareForTest({Hudson.class, StaplerRequest.class})
    public void testBuildExternalProperties() throws Exception {
        Job job = new FreeStyleProjectMock("job");
        StaplerRequest req = createMock(StaplerRequest.class);
        String javadocArchiverKey = "hudson-tasks-JavadocArchiver";
        JSONObject archiver = new JSONObject();
        archiver.put("javadoc_dir", "dir");
        archiver.put("keep_all", true);
        JSONObject json = new JSONObject();
        json.put(javadocArchiverKey, archiver);
        Hudson hudson = createMock(Hudson.class);
        Descriptor<Publisher> javadocDescriptor = new JavadocArchiver.DescriptorImpl();
        expect(hudson.getDescriptorOrDie(JavadocArchiver.class)).andReturn(javadocDescriptor);
        JavadocArchiver javadocArchiver = new JavadocArchiver("dir", true);
        expect(req.bindJSON(JavadocArchiver.class, archiver)).andReturn(javadocArchiver).anyTimes();

        List<Descriptor<Publisher>> descriptors = new ArrayList<Descriptor<Publisher>>();
        descriptors.add(javadocDescriptor);

        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replay(Hudson.class, hudson, req);

        assertNull(CascadingUtil.getExternalProjectProperty(job, javadocArchiverKey).getValue());
        CascadingUtil.buildExternalProperties(req, archiver, descriptors, job);
        assertNull(CascadingUtil.getExternalProjectProperty(job, javadocArchiverKey).getValue());
        CascadingUtil.buildExternalProperties(req, json, descriptors, job);
        assertNotNull(CascadingUtil.getExternalProjectProperty(job, javadocArchiverKey).getValue());
        verifyAll();
    }

    /**
     * Verify {@link CascadingUtil#isCascadableJobProperty(hudson.model.JobPropertyDescriptor)} method.
     */
    public void testIsCascadableJobProperty() {
        assertFalse(CascadingUtil.isCascadableJobProperty(new AuthorizationMatrixProperty.DescriptorImpl()));
        assertFalse(CascadingUtil.isCascadableJobProperty(new ParametersDefinitionProperty.DescriptorImpl()));
    }
}
