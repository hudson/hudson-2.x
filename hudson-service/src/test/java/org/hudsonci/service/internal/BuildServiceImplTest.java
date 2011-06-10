/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
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

package org.hudsonci.service.internal;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import hudson.model.Item;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;

import java.io.IOException;

import javax.servlet.ServletException;

import org.hudsonci.service.BuildNotFoundException;
import org.hudsonci.service.BuildService;
import org.hudsonci.service.ProjectService;
import org.hudsonci.service.SecurityService;
import org.hudsonci.service.ServiceRuntimeException;
import org.hudsonci.service.internal.BuildServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BuildServiceImplTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private SecurityService securityService;

    @Mock
    private AbstractProject<?, ?> project;

    @Mock
    private AbstractBuild<?, ?> build;

    private BuildService getInst() {
        return new BuildServiceImpl(projectService, securityService);
    }

    @Test
    public void getInstNotNull() {
        assertNotNull(getInst());
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullArg1() {
        new BuildServiceImpl(null, securityService);
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullArg2() {
        new BuildServiceImpl(projectService, null);
    }

    // deleteBuild --------------
    @Test(expected = NullPointerException.class)
    public void deleteBuildNullProject() {
        getInst().deleteBuild(null, 1);

    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteBuildNegativeBuildNumber() {
        getInst().deleteBuild(project, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteBuildZeroBuildNumber() {
        getInst().deleteBuild(project, 0);
    }

    @Test
    public void deleteBuildSecurity() throws IOException {

        // spy so that we can call real methods
        BuildService buildService = spy(getInst());

        // partial mocking, using doReturn to avoid type safety <?,?> BS
        doReturn(build).when(buildService).getBuild(project, 1);

        // test
        buildService.deleteBuild(project, 1);

        // verify, security before operation
        InOrder inOrder = inOrder(securityService, build);
        inOrder.verify(securityService).checkPermission(build, Run.DELETE);
        inOrder.verify(build).delete();

    }

    @Test(expected = ServiceRuntimeException.class)
    public void deleteBuildServiceRuntimeException() throws IOException {
        // spy so that we can call real methods
        BuildService buildService = spy(getInst());

        // partial mocking, using doReturn to avoid type safety <?,?> BS
        doReturn(build).when(buildService).getBuild(project, 1);
        doThrow(new IOException()).when(build).delete();

        // test
        buildService.deleteBuild(project, 1);

    }

    // keepBuild --------------

    @Test(expected = NullPointerException.class)
    public void keepBuildNullArg1() {
        getInst().keepBuild(null, 1, true);

    }

    @Test(expected = IllegalArgumentException.class)
    public void keepBuildNegativeBuildNumber() {
        getInst().keepBuild(project, -1, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void keepBuildZeroBuildNumber() {
        getInst().keepBuild(project, 0, true);
    }

    @Test
    public void keepBuildSecurity() throws IOException {

        // spy so that we can call real methods
        BuildService buildService = spy(getInst());

        // partial mocking, using doReturn to avoid type safety <?,?> BS
        doReturn(build).when(buildService).getBuild(project, 1);

        // test
        buildService.keepBuild(project, 1, true);

        // verify, security before operation
        InOrder inOrder = inOrder(securityService, build);
        inOrder.verify(securityService).checkPermission(build, Run.UPDATE);
        inOrder.verify(build).keepLog(false);

    }

    @Test(expected = ServiceRuntimeException.class)
    public void keepBuildServiceRuntimeException() throws IOException {
        // spy so that we can call real methods
        BuildService buildService = spy(getInst());

        // partial mocking, using doReturn to avoid type safety <?,?> BS
        doReturn(build).when(buildService).getBuild(project, 1);
        doThrow(new IOException()).when(build).keepLog(false);

        // test
        buildService.keepBuild(project, 1, true);

    }

    // getBuild(AbstractProject) -----------

    @Test(expected = NullPointerException.class)
    public void getBuildProjectNullProject() {
        getInst().getBuild((AbstractProject<?, ?>) null, 1);

    }

    @Test(expected = IllegalArgumentException.class)
    public void getBuildProjectBuildNumber() {
        getInst().getBuild(project, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getBuildProjectZeroBuildNumber() {
        getInst().getBuild(project, 0);
    }

    @Test
    public void getBuildByProjectSecurity() throws IOException {

        // spy so that we can call real methods
        BuildService buildService = spy(getInst());

        // partial mocking
        doReturn(build).when(project).getBuildByNumber(1);

        // test
        buildService.getBuild(project, 1);

        // verify
        verify(securityService).checkPermission(build, Item.READ);

    }

    @Test(expected = BuildNotFoundException.class)
    public void getBuildByProjectBuildNotFoundException() throws IOException {
        // spy so that we can call real methods
        getInst().getBuild(project, 1);
    }

    // getBuild(projectName) -----------

    @Test(expected = NullPointerException.class)
    public void getBuildProjectNameNullName() {
        getInst().getBuild((String) null, 1);

    }

    @Test(expected = IllegalArgumentException.class)
    public void getBuildProjectNameNegativeBuildNumber() {
        getInst().getBuild("projectName", -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getBuildProjectNameZeroBuildNumber() {
        getInst().getBuild("projectName", 0);
    }

    @Test
    public void getBuildByProjectNameSecurity() throws IOException {

        // spy so that we can call real methods
        BuildService buildService = spy(getInst());

        // partial mocking
        doReturn(build).when(project).getBuildByNumber(1);
        doReturn(project).when(projectService).getProject("projectName");

        // test
        buildService.getBuild("projectName", 1);

        // verify
        verify(securityService).checkPermission(build, Item.READ);

    }

    @Test(expected = BuildNotFoundException.class)
    public void getBuildByProjectNameBuildNotFoundException() throws IOException {
        doReturn(project).when(projectService).getProject("projectName");
        // spy so that we can call real methods
        getInst().getBuild("projectName", 1);
    }

    // findBuildByProject ------

    @Test(expected = NullPointerException.class)
    public void findBuildByProjectNullProject() {
        getInst().findBuild((AbstractProject<?, ?>) null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findBuildByProjectBuildNumber() {
        getInst().findBuild(project, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findBuildByProjectZeroBuildNumber() {
        getInst().findBuild(project, 0);
    }

    @Test
    public void findBuildByProjectSecurity() throws IOException {

        // spy so that we can call real methods
        BuildService buildService = spy(getInst());

        // partial mocking
        doReturn(build).when(project).getBuildByNumber(1);

        // test
        buildService.findBuild(project, 1);

        // verify
        verify(securityService).checkPermission(build, Item.READ);

    }

    @Test
    public void findBuildByProjectReturnNull() {
        assertNull(getInst().findBuild(project, 1));
    }

    // findBuildByProjectName -----

    @Test(expected = NullPointerException.class)
    public void findBuildProjectNameNullName() {
        getInst().findBuild((String) null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findBuildProjectNameNegativeBuildNumber() {
        getInst().findBuild("projectName", -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findBuildProjectNameZeroBuildNumber() {
        getInst().findBuild("projectName", 0);
    }

    @Test
    public void findBuildByProjectNameSecurity() throws IOException {

        // spy so that we can call real methods
        BuildService buildService = spy(getInst());

        // partial mocking
        doReturn(project).when(projectService).findProject("projectName");
        doReturn(build).when(project).getBuildByNumber(1);

        // test
        buildService.findBuild("projectName", 1);

        // verify
        verify(securityService).checkPermission(build, Item.READ);

    }

    @Test
    public void findBuildByProjectNameReturnNull() {
        assertNull(getInst().findBuild("projectName", 1));
    }


    // stopBuild -------------

    @Test(expected = NullPointerException.class)
    public void stopBuildByProjectNameNullName() {
        getInst().stopBuild(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void stopBuildByProjectNameNegativeBuildNumber() {
        getInst().stopBuild(project, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void stopBuildProjectNameZeroBuildNumber() {
        getInst().stopBuild(project, 0);
    }

    @Test
    public void stopBuildSecurity(){
        //intentionally blank as a note that security is performed internally in Hudson core.
    }

    @Test(expected = ServiceRuntimeException.class)
    public void stopBuildServiceRuntimeException1() throws IOException {
        BuildService inst = spy(getInst());

        doReturn(build).when(inst).getBuild(project, 1);
        try {
            doThrow(new IOException()).when(build).doStop(any(StaplerRequest.class), any(StaplerResponse.class));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        getInst().stopBuild(project, 1);
    }

    @Test(expected = ServiceRuntimeException.class)
    public void stopBuildServiceRuntimeException2() throws IOException {
        BuildService inst = spy(getInst());

        doReturn(build).when(inst).getBuild(project, 1);
        try {
            doThrow(new ServletException()).when(build).doStop(any(StaplerRequest.class), any(StaplerResponse.class));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        getInst().stopBuild(project, 1);
    }



}
