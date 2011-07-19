/*******************************************************************************
 *
 * Copyright (c) 2009, Yahoo!, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *      
 *
 *******************************************************************************/ 

package hudson.tasks.test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TouchBuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * A test case to make sure that the TestResult extension mechanism
 * is working properly. 
 */
public class TestResultExtensionTest extends HudsonTestCase {

    public void testTrivialRecorder() throws Exception {
        FreeStyleProject project = createFreeStyleProject("trivialtest");
        TrivialTestResultRecorder recorder = new TrivialTestResultRecorder();
        project.getPublishersList().add(recorder);
        project.getBuildersList().add(new TouchBuilder());

        FreeStyleBuild build = project.scheduleBuild2(0).get(5, TimeUnit.MINUTES); /* leave room for debugging*/
        assertBuildStatus(Result.SUCCESS, build);
        TrivialTestResultAction action = build.getAction(TrivialTestResultAction.class);
        assertNotNull("we should have an action", action);
        assertNotNull("parent action should have an owner", action.owner); 
        Object resultObject = action.getResult();
        assertNotNull("we should have a result");
        assertTrue("result should be an TestResult",
                resultObject instanceof TestResult);
        TestResult result = (TestResult) resultObject;
        AbstractBuild<?,?> ownerBuild = result.getOwner();
        assertNotNull("we should have an owner", ownerBuild);
        assertNotNull("we should have a list of test actions", result.getTestActions());

        // Validate that there are test results where I expect them to be:
        HudsonTestCase.WebClient wc = new HudsonTestCase.WebClient();
        HtmlPage projectPage = wc.getPage(project);
        assertGoodStatus(projectPage);
        HtmlPage testReportPage = wc.getPage(project, "/lastBuild/testReport/");
        assertGoodStatus(testReportPage);


    }
}


