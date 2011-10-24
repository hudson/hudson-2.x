package hudson;

/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Anton Kozak, Nikita Levyankov
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

import hudson.model.FreeStyleProject;
import hudson.model.FreeStyleProjectMock;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.verify;

/**
 * Test for {@link Functions}
 * <p/>
 * Date: 5/19/11
 *
 * @author Anton Kozak
 */
@RunWith(PowerMockRunner.class)
public class FunctionsTest {
    private static final String USER = "admin";
    private static final String TEMPLATE_NAME = "project_template";

    @Test
    @PrepareForTest(User.class)
    public void testIsAuthorTrue() throws Exception {
        mockStatic(User.class);
        User user = createMock(User.class);
        expect(user.getId()).andReturn(USER);
        expect(User.current()).andReturn(user);
        Job job = createMock(FreeStyleProject.class);
        expect(job.getCreatedBy()).andReturn(USER).times(2);
        replay(User.class, user, job);
        boolean result = Functions.isAuthor(job);
        verify(User.class, user, job);
        assertTrue(result);
    }

    @Test
    @PrepareForTest(User.class)
    public void testIsAuthorFalse() throws Exception {
        mockStatic(User.class);
        User user = createMock(User.class);
        expect(user.getId()).andReturn(USER);
        expect(User.current()).andReturn(user);
        Job job = createMock(FreeStyleProject.class);
        expect(job.getCreatedBy()).andReturn("user").times(2);
        replay(User.class, user, job);
        boolean result = Functions.isAuthor(job);
        verify(User.class, user, job);
        assertFalse(result);
    }

    @Test
    @PrepareForTest(User.class)
    public void testIsAuthorJobCreatedByNull() throws Exception {
        mockStatic(User.class);
        User user = createMock(User.class);
        expect(User.current()).andReturn(user);
        Job job = createMock(FreeStyleProject.class);
        expect(job.getCreatedBy()).andReturn(null);
        replay(User.class, user, job);
        boolean result = Functions.isAuthor(job);
        verify(User.class, user, job);
        assertFalse(result);
    }

    @Test
    @PrepareForTest(User.class)
    public void testIsAuthorUserIdNull() throws Exception {
        mockStatic(User.class);
        User user = createMock(User.class);
        expect(user.getId()).andReturn(null);
        expect(User.current()).andReturn(user);
        Job job = createMock(FreeStyleProject.class);
        expect(job.getCreatedBy()).andReturn(USER).times(2);
        replay(User.class, user, job);
        boolean result = Functions.isAuthor(job);
        verify(User.class, user, job);
        assertFalse(result);
    }

    @Test
    @PrepareForTest(User.class)
    public void testIsAuthorUserNull() throws Exception {
        mockStatic(User.class);
        expect(User.current()).andReturn(null);
        Job job = createMock(FreeStyleProject.class);
        replay(User.class, job);
        boolean result = Functions.isAuthor(job);
        verify(User.class, job);
        assertFalse(result);
    }

    @Test
    public void testGetTemplateWithNullTemplateName(){
        List<FreeStyleProject> items = new ArrayList<FreeStyleProject>();
        FreeStyleProject project = Functions.getItemByName(items, null);
        Assert.assertNull(project);
    }

    @Test
    public void testGetTemplateWithoutTemplates(){
        List<FreeStyleProject> items = new ArrayList<FreeStyleProject>();
        FreeStyleProject project = Functions.getItemByName(items, TEMPLATE_NAME);
        assertNull(project);
    }

    @Test
    public void testGetTemplatePresentTemplate(){
        FreeStyleProject parentProject = new FreeStyleProject(null, TEMPLATE_NAME);
        List<FreeStyleProject> items = new ArrayList<FreeStyleProject>();
        items.add(parentProject);
        FreeStyleProject project = Functions.getItemByName(items, TEMPLATE_NAME);
        assertNotNull(project);
    }

    @Test
    public void testUnlinkProjectFromCascadingParents() {
        //Prepare data
        FreeStyleProject project1 = new FreeStyleProjectMock("project1");
        FreeStyleProjectMock child1 = new FreeStyleProjectMock("child1");
        child1.setCascadingProject(project1);
        String cascadingName = "newCascadingProject";
        Functions.linkCascadingProjectsToChild(child1, cascadingName);

        //Can't unlink from null project
        assertFalse(Functions.unlinkProjectFromCascadingParents(null, cascadingName));
        //Can't unlink null cascading name
        assertFalse(Functions.unlinkProjectFromCascadingParents(project1, null));

        //Verify whether cascadingName is present in parent and child
        assertTrue(project1.getCascadingChildrenNames().contains(cascadingName));
        assertTrue(child1.getCascadingChildrenNames().contains(cascadingName));
        boolean result = Functions.unlinkProjectFromCascadingParents(child1, cascadingName);
        assertTrue(result);
        //Name should disappear from hierarchy.
        assertFalse(project1.getCascadingChildrenNames().contains(cascadingName));
        assertFalse(child1.getCascadingChildrenNames().contains(cascadingName));

        Functions.linkCascadingProjectsToChild(project1, cascadingName);
        assertTrue(project1.getCascadingChildrenNames().contains(cascadingName));
        result = Functions.unlinkProjectFromCascadingParents(child1, cascadingName);
        assertTrue(result);
        assertFalse(project1.getCascadingChildrenNames().contains(cascadingName));

    }

    @Test
    public void testLinkCascadingProjectsToChild() {
        FreeStyleProject project1 = new FreeStyleProjectMock("project1");
        FreeStyleProjectMock child1 = new FreeStyleProjectMock("child1");
        child1.setCascadingProject(project1);
        String cascadingName = "newCascadingProject";
        Functions.linkCascadingProjectsToChild(null, cascadingName);
        assertFalse(project1.getCascadingChildrenNames().contains(cascadingName));
        assertFalse(child1.getCascadingChildrenNames().contains(cascadingName));

        Functions.linkCascadingProjectsToChild(project1, cascadingName);
        assertTrue(project1.getCascadingChildrenNames().contains(cascadingName));

        project1 = new FreeStyleProjectMock("project1");
        child1 = new FreeStyleProjectMock("child1");
        child1.setCascadingProject(project1);
        Functions.linkCascadingProjectsToChild(child1, cascadingName);
        //Name should be included to all cascading parents up-hierarchy.
        assertTrue(project1.getCascadingChildrenNames().contains(cascadingName));
        assertTrue(child1.getCascadingChildrenNames().contains(cascadingName));
    }


    @Test
    public void testRenameCascadingChildLinks() {
        String oldName = "oldCascadingProject";
        String newName = "newCascadingProject";
        FreeStyleProject project1 = new FreeStyleProjectMock("project1");
        FreeStyleProjectMock project2 = new FreeStyleProjectMock("project2");
        FreeStyleProjectMock project3 = new FreeStyleProjectMock(oldName);
        project2.setCascadingProject(project1);
        Functions.linkCascadingProjectsToChild(project1, "project2");
        project3.setCascadingProject(project2);
        Functions.linkCascadingProjectsToChild(project2, oldName);
        assertTrue(project2.getCascadingChildrenNames().contains(oldName));
        assertTrue(project1.getCascadingChildrenNames().contains(oldName));

        Functions.renameCascadingChildLinks(project2, oldName, newName);

        assertTrue(project2.getCascadingChildrenNames().contains(newName));
        assertFalse(project2.getCascadingChildrenNames().contains(oldName));
        assertTrue(project1.getCascadingChildrenNames().contains(newName));
        assertFalse(project1.getCascadingChildrenNames().contains(oldName));
    }


    @Test
    @PrepareForTest(Hudson.class)
    public void testRenameCascadingParentLinks() {
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
        Functions.renameCascadingParentLinks(oldName, newName);
        verify(Hudson.class, hudson);
        assertEquals(newName, project2.getCascadingProjectName());
    }

    @Test
    public void testRenameCascadingParentLinksNullNames() {
        Functions.renameCascadingParentLinks(null, null);
    }

    @Test
    public void testRenameCascadingParentLinksEmptyNames() {
        Functions.renameCascadingParentLinks("", "");
    }

}
