package hudson.model;

/*
 * The MIT License
 *
 * Copyright (c) 2011, Oracle Corporation, Anton Kozak
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

import com.google.common.collect.Lists;
import hudson.matrix.MatrixProject;
import hudson.security.AuthorizationMatrixProperty;
import hudson.security.AuthorizationStrategy;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.ProjectMatrixAuthorizationStrategy;
import hudson.tasks.LogRotator;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verify;
import static org.powermock.api.easymock.PowerMock.verifyAll;

/**
 * Test for {@link FreeStyleProject}
 * <p/>
 * Date: 5/20/11
 *
 * @author Anton Kozak
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Hudson.class, User.class})
public class FreeStyleProjectTest {
    private static final String USER = "admin";

    @Test
    public void testOnCreatedFromScratch(){
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getNodes()).andReturn(Lists.<Node>newArrayList());
        AuthorizationStrategy authorizationStrategy = createMock(ProjectMatrixAuthorizationStrategy.class);
        expect(hudson.getAuthorizationStrategy()).andReturn(authorizationStrategy);
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        User user = createMock(User.class);
        expect(user.getId()).andReturn(USER).times(2);
        mockStatic(User.class);
        expect(User.current()).andReturn(user);
        replayAll();
        FreeStyleProject freeStyleProject = new FreeStyleProjectMock("testJob");
        freeStyleProject.onCreatedFromScratch();
        verifyAll();
        assertNotNull(freeStyleProject.getCreationTime());
        assertEquals(freeStyleProject.getCreatedBy(), USER);
        List properties = freeStyleProject.getAllProperties();
        assertEquals(properties.size(), 1);
        AuthorizationMatrixProperty property = (AuthorizationMatrixProperty)properties.get(0);
        assertEquals(property.getGrantedPermissions().keySet().size(), 7);
        assertNotNull(property.getGrantedPermissions().get(Item.CONFIGURE));
        assertTrue(property.getGrantedPermissions().get(Item.CONFIGURE).contains(USER));
    }

    @Test
    public void testOnCreatedFromScratchGlobalMatrixAuthorizationStrategy(){
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getNodes()).andReturn(Lists.<Node>newArrayList());
        AuthorizationStrategy authorizationStrategy = createMock(GlobalMatrixAuthorizationStrategy.class);
        expect(hudson.getAuthorizationStrategy()).andReturn(authorizationStrategy);
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        User user = createMock(User.class);
        expect(user.getId()).andReturn(USER).times(1);
        mockStatic(User.class);
        expect(User.current()).andReturn(user);
        replayAll();
        FreeStyleProject freeStyleProject = new FreeStyleProjectMock("testJob");
        freeStyleProject.onCreatedFromScratch();
        verifyAll();
        assertNotNull(freeStyleProject.getCreationTime());
        assertEquals(freeStyleProject.getCreatedBy(), USER);
        List properties = freeStyleProject.getAllProperties();
        assertEquals(properties.size(), 0);
    }

    @Test
    public void testOnCreatedFromScratchAnonymousAuthentication(){
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getNodes()).andReturn(Lists.<Node>newArrayList());
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        mockStatic(User.class);
        expect(User.current()).andReturn(null);
        replayAll();
        FreeStyleProject freeStyleProject = new FreeStyleProjectMock("testJob");
        freeStyleProject.onCreatedFromScratch();
        verifyAll();
        assertNotNull(freeStyleProject.getCreationTime());
        assertNull(freeStyleProject.getCreatedBy());
        List properties = freeStyleProject.getAllProperties();
        assertEquals(properties.size(), 0);
    }

    @Test
    public void testOnCopiedFrom(){
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getNodes()).andReturn(Lists.<Node>newArrayList()).times(2);
        AuthorizationStrategy authorizationStrategy = createMock(ProjectMatrixAuthorizationStrategy.class);
        expect(hudson.getAuthorizationStrategy()).andReturn(authorizationStrategy);
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        User user = createMock(User.class);
        expect(user.getId()).andReturn(USER).times(2);
        mockStatic(User.class);
        expect(User.current()).andReturn(user);
        replayAll();
        MatrixProject matrixProjectProject = new MatrixProject("matrixProject");
        FreeStyleProject freeStyleProject = new FreeStyleProjectMock("testJob");
        freeStyleProject.onCopiedFrom(matrixProjectProject);
        verifyAll();
        assertEquals(freeStyleProject.getNextBuildNumber(), 1);
        assertTrue(freeStyleProject.isHoldOffBuildUntilSave());
        assertNotNull(freeStyleProject.getCreationTime());
        assertEquals(freeStyleProject.getCreatedBy(), USER);
        List properties = freeStyleProject.getAllProperties();
        assertEquals(properties.size(), 1);
        AuthorizationMatrixProperty property = (AuthorizationMatrixProperty)properties.get(0);
        assertEquals(property.getGrantedPermissions().keySet().size(), 7);
        assertNotNull(property.getGrantedPermissions().get(Item.CONFIGURE));
        assertTrue(property.getGrantedPermissions().get(Item.CONFIGURE).contains(USER));
    }

    @Test
    public void testOnCopiedFromGlobalMatrixAuthorizationStrategy(){
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getNodes()).andReturn(Lists.<Node>newArrayList()).times(2);
        AuthorizationStrategy authorizationStrategy = createMock(GlobalMatrixAuthorizationStrategy.class);
        expect(hudson.getAuthorizationStrategy()).andReturn(authorizationStrategy);
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        User user = createMock(User.class);
        expect(user.getId()).andReturn(USER).times(1);
        mockStatic(User.class);
        expect(User.current()).andReturn(user);
        replayAll();
        MatrixProject matrixProjectProject = new MatrixProject("matrixProject");
        FreeStyleProject freeStyleProject = new FreeStyleProjectMock("testJob");
        freeStyleProject.onCopiedFrom(matrixProjectProject);
        verifyAll();
        assertEquals(freeStyleProject.getNextBuildNumber(), 1);
        assertTrue(freeStyleProject.isHoldOffBuildUntilSave());
        assertNotNull(freeStyleProject.getCreationTime());
        assertEquals(freeStyleProject.getCreatedBy(), USER);
        assertEquals(freeStyleProject.getAllProperties().size(), 0);
    }

    @Test
    public void testOnCopiedFromAnonymousAuthentication(){
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getNodes()).andReturn(Lists.<Node>newArrayList()).times(2);
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        mockStatic(User.class);
        expect(User.current()).andReturn(null);
        replayAll();
        MatrixProject matrixProjectProject = new MatrixProject("matrixProject");
        FreeStyleProject freeStyleProject = new FreeStyleProjectMock("testJob");
        freeStyleProject.onCopiedFrom(matrixProjectProject);
        verifyAll();
        assertEquals(freeStyleProject.getNextBuildNumber(), 1);
        assertTrue(freeStyleProject.isHoldOffBuildUntilSave());
        assertNotNull(freeStyleProject.getCreationTime());
        assertNull(freeStyleProject.getCreatedBy());
        List properties = freeStyleProject.getAllProperties();
        assertEquals(properties.size(), 0);
    }


    @Test
    public void testGetLogRotatorFromParent(){
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setLogRotator(new LogRotator(10,11,12,13));

        FreeStyleProject childProject1 = new FreeStyleProjectMock("child1");
        childProject1.setTemplate(parentProject);
        LogRotator result = childProject1.getLogRotator();
        assertNotNull(result);
        assertEquals(result.getDaysToKeep(), 10);
    }

    @Test
    public void testGetLogRotatorFromChild(){
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setLogRotator(new LogRotator(10,10,10,10));

        FreeStyleProject childProject1 = new FreeStyleProjectMock("child1");
        childProject1.setLogRotator(new LogRotator(20, 20, 20, 20));
        childProject1.setTemplate(parentProject);
        LogRotator result = childProject1.getLogRotator();
        assertNotNull(result);
        assertEquals(result.getDaysToKeep(), 20);
    }

    @Test
    public void testSetLogRotatorValueEqualsWithParent(){
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setLogRotator(new LogRotator(10,11,12,13));

        FreeStyleProject childProject1 = new FreeStyleProjectMock("child1");
        childProject1.setTemplate(parentProject);
        childProject1.setLogRotator(new LogRotator(10, 11, 12, 13));
        childProject1.setTemplate(null); // else log rotator will be taken from parent
        assertNull(childProject1.getLogRotator());
    }

    @Test
    public void testSetLogRotatorParentNull(){
        FreeStyleProject childProject1 = new FreeStyleProjectMock("child1");
        childProject1.setLogRotator(new LogRotator(10, 11, 12, 13));
        assertNotNull(childProject1.getLogRotator());
        assertEquals(childProject1.getLogRotator().getDaysToKeep(), 10);
    }

    @Test
    public void testSetCustomWorkspaceValueEqualsWithParent() throws IOException{
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.allowSave.set(false);
        String customWorkspace = "/tmp";
        parentProject.setCustomWorkspace(customWorkspace);
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.allowSave.set(false);
        childProject.setTemplate(parentProject);
        childProject.setCustomWorkspace(customWorkspace);
        childProject.setTemplate(null);
        assertNull(childProject.getCustomWorkspace());
    }

    @Test
    public void testSetCustomWorkspaceValueNotEqualsWithParent() throws IOException{
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.allowSave.set(false);
        String parentCustomWorkspace = "/tmp";
        String childCustomWorkspace = "/tmp1";
        parentProject.setCustomWorkspace(parentCustomWorkspace);
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.allowSave.set(false);
        childProject.setTemplate(parentProject);
        childProject.setCustomWorkspace(childCustomWorkspace);
        assertEquals(childCustomWorkspace, childProject.getCustomWorkspace());
    }

    @Test
    public void testSetCustomWorkspaceValueParentNull() throws IOException{
        String childCustomWorkspace = "/tmp";
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.allowSave.set(false);
        childProject.setCustomWorkspace(childCustomWorkspace);
        assertEquals(childCustomWorkspace, childProject.getCustomWorkspace());
    }

    @Test
    public void testGetCustomWorkspace() throws IOException{
        String customWorkspace = "/tmp";
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.allowSave.set(false);
        childProject.setCustomWorkspace(customWorkspace);
        assertEquals(customWorkspace, childProject.getCustomWorkspace());

        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.allowSave.set(false);
        parentProject.setCustomWorkspace(customWorkspace);
        childProject.setCustomWorkspace(" ");
        childProject.setTemplate(parentProject);
        assertEquals(customWorkspace, childProject.getCustomWorkspace());
        parentProject.setCustomWorkspace("  ");
        assertNull(childProject.getCustomWorkspace());
    }

    @Test
    public void testSetJdkValueEqualsWithParent() throws IOException{
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.allowSave.set(false);
        String jdkName = "sun-java5-jdk32";
        parentProject.setJDK(jdkName);
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.allowSave.set(false);
        childProject.setTemplate(parentProject);
        childProject.setJDK(jdkName);
        childProject.setTemplate(null);
        assertNull(childProject.getJDKName());
    }

    @Test
    public void testSetJdkValueNotEqualsWithParent() throws IOException{
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.allowSave.set(false);
        String parentJdkName = "sun-java5-jdk32";
        String childJdkName = "sun-java6-jdk32";
        parentProject.setJDK(parentJdkName);
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.allowSave.set(false);
        childProject.setTemplate(parentProject);
        childProject.setJDK(childJdkName);
        assertEquals(childJdkName, childProject.getJDKName());
    }

    @Test
    public void testSetJdkValueParentNull() throws IOException{
        String childJdkName = "sun-java6-jdk32";
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.allowSave.set(false);
        childProject.setJDK(childJdkName);
        assertEquals(childJdkName, childProject.getJDKName());
    }

    @Test
    public void testGetJdkName() throws IOException{
        String JdkName = "sun-java6-jdk32";
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.allowSave.set(false);
        childProject.setJDK(JdkName);
        assertEquals(JdkName, childProject.getJDKName());

        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.allowSave.set(false);
        parentProject.setJDK(JdkName);
        childProject.setJDK(" ");
        childProject.setTemplate(parentProject);
        assertEquals(JdkName, childProject.getJDKName());
        parentProject.setJDK("  ");
        assertNull(childProject.getJDKName());
    }

    @Test
    public void testSetQuietPeriodEqualsWithParent() throws IOException {
        String quietPeriod = "10";
        int globalQuietPeriod = 4;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.allowSave.set(false);
        parentProject.setQuietPeriod(quietPeriod);
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.allowSave.set(false);
        childProject.setTemplate(parentProject);
        childProject.setQuietPeriod(quietPeriod);
        childProject.setTemplate(null);

        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getQuietPeriod()).andReturn(globalQuietPeriod);
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();
        assertEquals(childProject.getQuietPeriod(), globalQuietPeriod);
        verifyAll();
    }

    @Test
    public void testSetQuietPeriodNotEqualsWithParent() throws IOException{
        String parentQuietPeriod = "10";
        String childQuietPeriod = "11";
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.allowSave.set(false);
        parentProject.setQuietPeriod(parentQuietPeriod);
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.allowSave.set(false);
        childProject.setTemplate(parentProject);
        childProject.setQuietPeriod(childQuietPeriod);

        Hudson hudson = createMock(Hudson.class);
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();
        assertEquals( childProject.getQuietPeriod(), Integer.parseInt(childQuietPeriod));
        verifyAll();
    }

    @Test
    public void testSetQuietPeriodParentNull() throws IOException{
        String quietPeriod = "10";
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.allowSave.set(false);
        childProject.setQuietPeriod(quietPeriod);
        assertEquals(Integer.parseInt(quietPeriod), childProject.getQuietPeriod());
    }

    @Test
    public void testSetInvalidQuietPeriod() throws IOException{
        String quietPeriod = "asd10asdasd";
        int globalQuietPeriod = 4;
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.allowSave.set(false);
        childProject.setQuietPeriod(quietPeriod);
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getQuietPeriod()).andReturn(globalQuietPeriod).anyTimes();
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();
        assertEquals(globalQuietPeriod, childProject.getQuietPeriod());
        verifyAll();
    }

    @Test
    public void testGetQuietPeriod() throws IOException{
        String quietPeriodString = "10";
        int globalQuietPeriod = 4;
        int quietPeriod = Integer.parseInt(quietPeriodString);
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getQuietPeriod()).andReturn(globalQuietPeriod).anyTimes();
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();

        childProject.allowSave.set(false);
        childProject.setQuietPeriod(quietPeriodString);
        assertEquals(quietPeriod, childProject.getQuietPeriod());

        parentProject.allowSave.set(false);
        parentProject.setQuietPeriod(quietPeriodString);
        childProject.setQuietPeriod(" ");
        childProject.setTemplate(parentProject);
        assertEquals(childProject.getQuietPeriod(), quietPeriod);

        parentProject.setQuietPeriod("  ");
        assertEquals(globalQuietPeriod, childProject.getQuietPeriod());
        verifyAll();
    }

    private class FreeStyleProjectMock extends FreeStyleProject {

        private FreeStyleProjectMock(String name) {
            super((ItemGroup)null, name);
        }

        @Override
        protected void updateTransientActions() {
        }
    }
}
