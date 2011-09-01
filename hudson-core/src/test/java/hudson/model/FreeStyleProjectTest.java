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
 * Anton Kozak
 *
 *******************************************************************************/ 

package hudson.model;

import com.google.common.collect.Lists;
import hudson.matrix.MatrixProject;
import hudson.security.AuthorizationMatrixProperty;
import hudson.security.AuthorizationStrategy;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.ProjectMatrixAuthorizationStrategy;
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
    public void testOnCreatedFromScratch(){
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
        FreeStyleProject freeStyleProject = new FreeStyleProject(matrixProjectProject, "testJob"){
            @Override
            protected void updateTransientActions() {
            }
        };
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
        FreeStyleProject freeStyleProject = new FreeStyleProject(matrixProjectProject, "testJob"){
            @Override
            protected void updateTransientActions() {
            }
        };
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
        expect(hudson.getNodes()).andReturn(Lists.<Node>newArrayList()).times(2);
        mockStatic(Hudson.class);
        expect(Hudson.getInstance()).andReturn(hudson).anyTimes();
        mockStatic(User.class);
        expect(User.current()).andReturn(null);
        replayAll();
        MatrixProject matrixProjectProject = new MatrixProject("matrixProject");
        FreeStyleProject freeStyleProject = new FreeStyleProject(matrixProjectProject, "testJob"){
            @Override
            protected void updateTransientActions() {
            }
        };
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
        FreeStyleProject freeStyleProject = new FreeStyleProject(matrixProjectProject, "testJob"){
            @Override
            protected void updateTransientActions() {
            }
        };
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
        FreeStyleProject freeStyleProject = new FreeStyleProject(matrixProjectProject, "testJob"){
            @Override
            protected void updateTransientActions() {
            }
        };
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
        FreeStyleProject freeStyleProject = new FreeStyleProject(matrixProjectProject, "testJob"){
            @Override
            protected void updateTransientActions() {
            }
        };
        freeStyleProject.onCopiedFrom(matrixProjectProject);
        verifyAll();
        assertEquals(freeStyleProject.getNextBuildNumber(), 1);
        assertTrue(freeStyleProject.isHoldOffBuildUntilSave());
        assertNotNull(freeStyleProject.getCreationTime());
        assertNull(freeStyleProject.getCreatedBy());
        List properties = freeStyleProject.getAllProperties();
        assertEquals(properties.size(), 0);
    }
}
