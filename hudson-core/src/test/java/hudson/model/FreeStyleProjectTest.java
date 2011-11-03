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
import hudson.util.CascadingUtil;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
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
    public void testOnCreatedFromScratch() {
        Hudson hudson = createMock(Hudson.class);
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
        AuthorizationMatrixProperty property = (AuthorizationMatrixProperty) properties.get(0);
        assertEquals(property.getGrantedPermissions().keySet().size(), 7);
        assertNotNull(property.getGrantedPermissions().get(Item.CONFIGURE));
        assertTrue(property.getGrantedPermissions().get(Item.CONFIGURE).contains(USER));
    }

    @Test
    public void testOnCreatedFromScratchGlobalMatrixAuthorizationStrategy() {
        Hudson hudson = createMock(Hudson.class);
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
    public void testOnCreatedFromScratchAnonymousAuthentication() {
        Hudson hudson = createMock(Hudson.class);
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
    public void testOnCopiedFrom() {
        Hudson hudson = createMock(Hudson.class);
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
        AuthorizationMatrixProperty property = (AuthorizationMatrixProperty) properties.get(0);
        assertEquals(property.getGrantedPermissions().keySet().size(), 7);
        assertNotNull(property.getGrantedPermissions().get(Item.CONFIGURE));
        assertTrue(property.getGrantedPermissions().get(Item.CONFIGURE).contains(USER));
    }

    @Test
    public void testOnCopiedFromGlobalMatrixAuthorizationStrategy() {
        Hudson hudson = createMock(Hudson.class);
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
    public void testOnCopiedFromAnonymousAuthentication() {
        Hudson hudson = createMock(Hudson.class);
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
    public void testGetLogRotatorFromParent() {
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setLogRotator(new LogRotator(10, 11, 12, 13));

        FreeStyleProjectMock childProject1 = new FreeStyleProjectMock("child1");
        childProject1.setCascadingProject(parentProject);
        LogRotator result = childProject1.getLogRotator();
        assertNotNull(result);
        assertEquals(result.getDaysToKeep(), 10);
    }

    @Test
    public void testGetLogRotatorFromChild() {
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setLogRotator(new LogRotator(10, 10, 10, 10));

        FreeStyleProjectMock childProject1 = new FreeStyleProjectMock("child1");
        childProject1.setLogRotator(new LogRotator(20, 20, 20, 20));
        childProject1.setCascadingProject(parentProject);
        LogRotator result = childProject1.getLogRotator();
        assertNotNull(result);
        assertEquals(result.getDaysToKeep(), 20);
    }

    @Test
    public void testSetLogRotatorValueEqualsWithParent() {
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setLogRotator(new LogRotator(10, 11, 12, 13));

        FreeStyleProjectMock childProject1 = new FreeStyleProjectMock("child1");
        childProject1.setCascadingProject(parentProject);
        childProject1.setLogRotator(new LogRotator(10, 11, 12, 13));
        childProject1.setCascadingProject(null); // else log rotator will be taken from parent
        assertNull(childProject1.getLogRotator());
    }

    @Test
    public void testSetLogRotatorParentNull() {
        FreeStyleProject childProject1 = new FreeStyleProjectMock("child1");
        childProject1.setLogRotator(new LogRotator(10, 11, 12, 13));
        assertNotNull(childProject1.getLogRotator());
        assertEquals(childProject1.getLogRotator().getDaysToKeep(), 10);
    }

    @Test
    public void testSetCustomWorkspaceValueEqualsWithParent() throws IOException {
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        String customWorkspace = "/tmp";
        parentProject.setCustomWorkspace(customWorkspace);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setCustomWorkspace(customWorkspace);
        childProject.setCascadingProject(null);
        assertNull(childProject.getCustomWorkspace());
    }

    @Test
    public void testSetCustomWorkspaceValueNotEqualsWithParent() throws IOException {
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        String parentCustomWorkspace = "/tmp";
        String childCustomWorkspace = "/tmp1";
        parentProject.setCustomWorkspace(parentCustomWorkspace);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setCustomWorkspace(childCustomWorkspace);
        assertEquals(childCustomWorkspace, childProject.getCustomWorkspace());
    }

    @Test
    public void testSetCustomWorkspaceValueParentNull() throws IOException {
        String childCustomWorkspace = "/tmp";
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.setCustomWorkspace(childCustomWorkspace);
        assertEquals(childCustomWorkspace, childProject.getCustomWorkspace());
    }

    @Test
    public void testGetCustomWorkspace() throws IOException {
        String customWorkspace = "/tmp";
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCustomWorkspace(customWorkspace);
        assertEquals(customWorkspace, childProject.getCustomWorkspace());

        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setCustomWorkspace(customWorkspace);
        childProject.setCustomWorkspace(" ");
        childProject.setCascadingProject(parentProject);
        assertEquals(customWorkspace, childProject.getCustomWorkspace());
        parentProject.setCustomWorkspace("  ");
        assertNull(childProject.getCustomWorkspace());
    }

    @Test
    public void testSetJdkValueEqualsWithParent() throws IOException {
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        String jdkName = "sun-java5-jdk32";
        parentProject.setJDK(jdkName);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setJDK(jdkName);
        childProject.setCascadingProject(null);
        assertNull(childProject.getJDKName());
    }

    @Test
    public void testSetJdkValueNotEqualsWithParent() throws IOException {
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        String parentJdkName = "sun-java5-jdk32";
        String childJdkName = "sun-java6-jdk32";
        parentProject.setJDK(parentJdkName);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setJDK(childJdkName);
        assertEquals(childJdkName, childProject.getJDKName());
    }

    @Test
    public void testSetJdkValueParentNull() throws IOException {
        String childJdkName = "sun-java6-jdk32";
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.setJDK(childJdkName);
        assertEquals(childJdkName, childProject.getJDKName());
    }

    @Test
    public void testGetJdkName() throws IOException {
        String JdkName = "sun-java6-jdk32";
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setJDK(JdkName);
        assertEquals(JdkName, childProject.getJDKName());

        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setJDK(JdkName);
        childProject.setJDK(" ");
        childProject.setCascadingProject(parentProject);
        assertEquals(JdkName, childProject.getJDKName());
        parentProject.setJDK("  ");
        assertNull(childProject.getJDKName());
    }

    @Test
    public void testSetQuietPeriodEqualsWithParent() throws IOException {
        String quietPeriod = "10";
        int globalQuietPeriod = 4;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getQuietPeriod()).andReturn(globalQuietPeriod).anyTimes();
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();
        parentProject.setQuietPeriod(quietPeriod);
        childProject.setCascadingProject(parentProject);
        childProject.setQuietPeriod(quietPeriod);
        childProject.setCascadingProject(null);
        assertEquals(childProject.getQuietPeriod(), globalQuietPeriod);
        verifyAll();
    }

    @Test
    public void testSetQuietPeriodEqualsGlobal() throws IOException {
        String quietPeriod = "4";
        int globalQuietPeriod = 4;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getQuietPeriod()).andReturn(globalQuietPeriod).anyTimes();
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();
        parentProject.setQuietPeriod(quietPeriod);
        childProject.setCascadingProject(parentProject);
        childProject.setQuietPeriod(quietPeriod);
        childProject.setCascadingProject(null);
        assertEquals(childProject.getQuietPeriod(), globalQuietPeriod);
        verifyAll();
    }

    @Test
    public void testSetQuietPeriodNotEqualsWithParent() throws IOException {
        String parentQuietPeriod = "10";
        String childQuietPeriod = "11";
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setQuietPeriod(parentQuietPeriod);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setQuietPeriod(childQuietPeriod);

        Hudson hudson = createMock(Hudson.class);
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();
        assertEquals(childProject.getQuietPeriod(), Integer.parseInt(childQuietPeriod));
        verifyAll();
    }

    @Test
    public void testSetQuietPeriodParentNull() throws IOException {
        String quietPeriod = "10";
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.setQuietPeriod(quietPeriod);
        assertEquals(Integer.parseInt(quietPeriod), childProject.getQuietPeriod());
    }

    @Test
    public void testSetInvalidQuietPeriod() throws IOException {
        String quietPeriod = "asd10asdasd";
        int globalQuietPeriod = 4;
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
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
    public void testGetQuietPeriod() throws IOException {
        String quietPeriodString = "10";
        int globalQuietPeriod = 4;
        int quietPeriod = Integer.parseInt(quietPeriodString);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getQuietPeriod()).andReturn(globalQuietPeriod).anyTimes();
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();

        childProject.setQuietPeriod(quietPeriodString);
        assertEquals(quietPeriod, childProject.getQuietPeriod());

        parentProject.setQuietPeriod(quietPeriodString);
        childProject.setQuietPeriod(" ");
        childProject.setCascadingProject(parentProject);
        assertEquals(childProject.getQuietPeriod(), quietPeriod);

        parentProject.setQuietPeriod("  ");
        assertEquals(globalQuietPeriod, childProject.getQuietPeriod());
        verifyAll();
    }

    @Test
    public void testSetScmCheckoutRetryCountEqualsWithParent() throws IOException {
        String scmCheckoutRetryCount = "10";
        int globalScmCheckoutRetryCount = 4;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setScmCheckoutRetryCount(scmCheckoutRetryCount);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getScmCheckoutRetryCount()).andReturn(globalScmCheckoutRetryCount);
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();
        assertEquals(childProject.getScmCheckoutRetryCount(), globalScmCheckoutRetryCount);
        childProject.setCascadingProject(parentProject);
        childProject.setScmCheckoutRetryCount(scmCheckoutRetryCount);
        assertEquals(childProject.getScmCheckoutRetryCount(), Integer.parseInt(scmCheckoutRetryCount));
        verifyAll();
    }

    @Test
    public void testSetScmCheckoutRetryCountNotEqualsWithParent() throws IOException {
        String parentScmCheckoutRetryCount = "10";
        String childScmCheckoutRetryCount = "11";
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setScmCheckoutRetryCount(parentScmCheckoutRetryCount);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setScmCheckoutRetryCount(childScmCheckoutRetryCount);

        Hudson hudson = createMock(Hudson.class);
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();
        assertEquals(childProject.getScmCheckoutRetryCount(), Integer.parseInt(childScmCheckoutRetryCount));
        verifyAll();
    }

    @Test
    public void testSetScmCheckoutRetryCountParentNull() throws IOException {
        String scmCheckoutRetryCount = "10";
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.setScmCheckoutRetryCount(scmCheckoutRetryCount);
        assertEquals(Integer.parseInt(scmCheckoutRetryCount), childProject.getScmCheckoutRetryCount());
    }

    @Test
    public void testSetInvalidScmCheckoutRetryCount() throws IOException {
        String scmCheckoutRetryCount = "asd10asdasd";
        int globalScmCheckoutRetryCount = 4;
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.setScmCheckoutRetryCount(scmCheckoutRetryCount);
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getScmCheckoutRetryCount()).andReturn(globalScmCheckoutRetryCount).anyTimes();
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();
        assertEquals(globalScmCheckoutRetryCount, childProject.getScmCheckoutRetryCount());
        verifyAll();
    }

    @Test
    public void testGetScmCheckoutRetryCount() throws IOException {
        String scmCheckoutRetryCountString = "10";
        int globalScmCheckoutRetryCount = 4;
        int scmCheckoutRetryCount = Integer.parseInt(scmCheckoutRetryCountString);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        Hudson hudson = createMock(Hudson.class);
        expect(hudson.getScmCheckoutRetryCount()).andReturn(globalScmCheckoutRetryCount).anyTimes();
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        replayAll();

        childProject.setScmCheckoutRetryCount(scmCheckoutRetryCountString);
        assertEquals(scmCheckoutRetryCount, childProject.getScmCheckoutRetryCount());

        parentProject.setScmCheckoutRetryCount(scmCheckoutRetryCountString);
        childProject.setScmCheckoutRetryCount(" ");
        childProject.setCascadingProject(parentProject);
        assertEquals(childProject.getScmCheckoutRetryCount(), scmCheckoutRetryCount);

        parentProject.setScmCheckoutRetryCount("  ");
        assertEquals(globalScmCheckoutRetryCount, childProject.getScmCheckoutRetryCount());
        verifyAll();
    }

    @Test
    public void testSetBlockBuildWhenDownstreamBuildingEqualsWithParent() throws IOException {
        Boolean blockBuildWhenDownstreamBuilding = true;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setBlockBuildWhenDownstreamBuilding(blockBuildWhenDownstreamBuilding);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setBlockBuildWhenDownstreamBuilding(blockBuildWhenDownstreamBuilding);
        assertFalse(childProject.blockBuildWhenDownstreamBuilding);
    }

    @Test
    public void testSetBlockBuildWhenDownstreamBuildingNotEqualsWithParent() throws IOException {
        Boolean childBlockBuildWhenDownstreamBuilding = false;
        Boolean parentBlockBuildWhenDownstreamBuilding = true;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setBlockBuildWhenDownstreamBuilding(parentBlockBuildWhenDownstreamBuilding);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setBlockBuildWhenDownstreamBuilding(childBlockBuildWhenDownstreamBuilding);
        //if child value is not equals to parent one, field should be populated
        assertNotNull(childProject.blockBuildWhenDownstreamBuilding);
    }

    @Test
    public void testSetBlockBuildWhenDownstreamBuildingParentNull() throws IOException {
        Boolean blockBuildWhenDownstreamBuilding = true;
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.setBlockBuildWhenDownstreamBuilding(blockBuildWhenDownstreamBuilding);
        //if parent is not set, value should be populated according to existing logic
        assertEquals(blockBuildWhenDownstreamBuilding, CascadingUtil.getBooleanProjectProperty(childProject,
            AbstractProject.BLOCK_BUILD_WHEN_DOWNSTREAM_BUILDING_PROPERTY_NAME).getOriginalValue());
    }

    @Test
    public void testBlockBuildWhenDownstreamBuilding() throws IOException {
        boolean childBlockBuildWhenDownstreamBuilding = false;
        boolean parentBlockBuildWhenDownstreamBuilding = true;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setBlockBuildWhenDownstreamBuilding(parentBlockBuildWhenDownstreamBuilding);
        assertEquals(parentBlockBuildWhenDownstreamBuilding, parentProject.blockBuildWhenDownstreamBuilding());
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        //Set equal to parent in order to inherit from cascading project
        childProject.setCascadingProject(parentProject);
        childProject.setBlockBuildWhenDownstreamBuilding(parentBlockBuildWhenDownstreamBuilding);
        //Value should be taken from cascadingProject
        assertEquals(parentBlockBuildWhenDownstreamBuilding, childProject.blockBuildWhenDownstreamBuilding());
        childProject.setBlockBuildWhenDownstreamBuilding(childBlockBuildWhenDownstreamBuilding);
        //Child value is not equals to parent - override value in child.
        assertEquals(childBlockBuildWhenDownstreamBuilding, childProject.blockBuildWhenDownstreamBuilding());
    }

    @Test
    public void testSetBlockBuildWhenUpstreamBuildingEqualsWithParent() throws IOException {
        boolean blockBuildWhenUpstreamBuilding = true;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setBlockBuildWhenUpstreamBuilding(blockBuildWhenUpstreamBuilding);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setBlockBuildWhenUpstreamBuilding(blockBuildWhenUpstreamBuilding);
        assertFalse(CascadingUtil.getBooleanProjectProperty(childProject,
            AbstractProject.BLOCK_BUILD_WHEN_UPSTREAM_BUILDING_PROPERTY_NAME).getOriginalValue());
    }

    @Test
    public void testSetBlockBuildWhenUpstreamBuildingNotEqualsWithParent() throws IOException {
        Boolean childBlockBuildWhenUpstreamBuilding = false;
        Boolean parentBlockBuildWhenUpstreamBuilding = true;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setBlockBuildWhenUpstreamBuilding(parentBlockBuildWhenUpstreamBuilding);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setBlockBuildWhenUpstreamBuilding(childBlockBuildWhenUpstreamBuilding);
        //if child value is not equals to parent one, field should be populated
        assertNotNull(childProject.blockBuildWhenUpstreamBuilding);
    }

    @Test
    public void testSetBlockBuildWhenUpstreamBuildingParentNull() throws IOException {
        Boolean blockBuildWhenUpstreamBuilding = true;
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.setBlockBuildWhenUpstreamBuilding(blockBuildWhenUpstreamBuilding);
        //if parent is not set, value should be populated according to existing logic
        assertEquals(blockBuildWhenUpstreamBuilding, CascadingUtil.getBooleanProjectProperty(childProject,
            AbstractProject.BLOCK_BUILD_WHEN_UPSTREAM_BUILDING_PROPERTY_NAME).getOriginalValue());
    }

    @Test
    public void testBlockBuildWhenUpstreamBuilding() throws IOException {
        boolean childBlockBuildWhenUpstreamBuilding = false;
        boolean parentBlockBuildWhenUpstreamBuilding = true;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setBlockBuildWhenUpstreamBuilding(parentBlockBuildWhenUpstreamBuilding);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        //Set equal to parent in order to inherit from cascading project
        childProject.setBlockBuildWhenUpstreamBuilding(parentBlockBuildWhenUpstreamBuilding);
        childProject.setCascadingProject(parentProject);
        //Value should be taken from cascadingProject
        assertEquals(parentBlockBuildWhenUpstreamBuilding, childProject.blockBuildWhenUpstreamBuilding());
        childProject.setBlockBuildWhenUpstreamBuilding(childBlockBuildWhenUpstreamBuilding);
        //Child value is not equals to parent - override value in child.
        assertEquals(childBlockBuildWhenUpstreamBuilding, childProject.blockBuildWhenUpstreamBuilding());
    }

    //    ---
    @Test
    public void testSetCleanWorkspaceRequiredEqualsWithParent() throws IOException {
        boolean cleanWorkspaceRequired = true;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setCleanWorkspaceRequired(cleanWorkspaceRequired);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setCleanWorkspaceRequired(cleanWorkspaceRequired);
        assertFalse(CascadingUtil.getBooleanProjectProperty(childProject,
            AbstractProject.CLEAN_WORKSPACE_REQUIRED_PROPERTY_NAME).getOriginalValue());
    }

    @Test
    public void testSetCleanWorkspaceRequiredNotEqualsWithParent() throws IOException {
        Boolean childCleanWorkspaceRequired = false;
        Boolean parentCleanWorkspaceRequired = true;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setCleanWorkspaceRequired(parentCleanWorkspaceRequired);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setCleanWorkspaceRequired(childCleanWorkspaceRequired);
        //if child value is not equals to parent one, field should be populated
        assertFalse(CascadingUtil.getBooleanProjectProperty(childProject,
            AbstractProject.CLEAN_WORKSPACE_REQUIRED_PROPERTY_NAME).getOriginalValue());
    }

    @Test
    public void testSetCleanWorkspaceRequiredParentNull() throws IOException {
        Boolean cleanWorkspaceRequired = true;
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.setCleanWorkspaceRequired(cleanWorkspaceRequired);
        //if parent is not set, value should be populated according to existing logic
        assertEquals(cleanWorkspaceRequired, CascadingUtil.getBooleanProjectProperty(childProject,
            AbstractProject.CLEAN_WORKSPACE_REQUIRED_PROPERTY_NAME).getOriginalValue());
    }

    @Test
    public void testIsCleanWorkspaceRequired() throws IOException {
        boolean childCleanWorkspaceRequired = false;
        boolean parentCleanWorkspaceRequired = true;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setCleanWorkspaceRequired(parentCleanWorkspaceRequired);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCleanWorkspaceRequired(parentCleanWorkspaceRequired);
        childProject.setCascadingProject(parentProject);
        //Value should be taken from cascadingProject
        assertEquals(parentCleanWorkspaceRequired, childProject.isCleanWorkspaceRequired());
        childProject.setCleanWorkspaceRequired(childCleanWorkspaceRequired);
        //Child value is not equals to parent - override value in child.
        assertEquals(childCleanWorkspaceRequired, childProject.isCleanWorkspaceRequired());
    }

    @Test
    public void testSetConcurrentBuildEqualsWithParent() throws IOException {
        Boolean concurrentBuild = true;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setConcurrentBuild(concurrentBuild);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setConcurrentBuild(concurrentBuild);
        assertFalse(CascadingUtil.getBooleanProjectProperty(childProject,
            AbstractProject.CONCURRENT_BUILD_PROPERTY_NAME).getOriginalValue());
    }

    @Test
    public void testSetConcurrentBuildNotEqualsWithParent() throws IOException {
        Boolean childConcurrentBuild = false;
        Boolean parentConcurrentBuild = true;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setConcurrentBuild(parentConcurrentBuild);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setConcurrentBuild(childConcurrentBuild);
        //if child value is not equals to parent one, field should be populated
        assertEquals(childConcurrentBuild, CascadingUtil.getBooleanProjectProperty(childProject,
            AbstractProject.CONCURRENT_BUILD_PROPERTY_NAME).getOriginalValue());
    }

    @Test
    public void testSetConcurrentBuildParentNull() throws IOException {
        Boolean concurrentBuild = true;
        FreeStyleProject childProject = new FreeStyleProjectMock("child");
        childProject.setConcurrentBuild(concurrentBuild);
        //if parent is not set, value should be populated according to existing logic
        assertEquals(concurrentBuild, CascadingUtil.getBooleanProjectProperty(childProject,
            AbstractProject.CONCURRENT_BUILD_PROPERTY_NAME).getOriginalValue());
    }

    @Test
    public void testIsConcurrentBuild() throws IOException {
        boolean childConcurrentBuild = false;
        boolean parentConcurrentBuild = true;
        FreeStyleProject parentProject = new FreeStyleProjectMock("parent");
        parentProject.setConcurrentBuild(parentConcurrentBuild);
        FreeStyleProjectMock childProject = new FreeStyleProjectMock("child");
        childProject.setCascadingProject(parentProject);
        childProject.setConcurrentBuild(true);
        //Value should be taken from cascadingProject
        assertEquals(parentConcurrentBuild, childProject.isConcurrentBuild());
        childProject.setConcurrentBuild(childConcurrentBuild);
        //Child value is not equals to parent - override value in child.
        assertEquals(childConcurrentBuild, childProject.isConcurrentBuild());
    }
}
