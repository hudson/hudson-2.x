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
*    Kohsuke Kawaguchi, Bruce Chapman
 *     
 *
 *******************************************************************************/ 

package org.jvnet.hudson.main;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.Shell;
import hudson.tasks.BatchFile;
import org.apache.commons.io.FileUtils;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Experimenting with Hudson test suite.
 */
public class AppTest extends HudsonTestCase
{
    public void test1() throws Exception {
        meat();
    }

    public void test2() throws Exception {
        meat();
    }

    private void meat() throws IOException, InterruptedException, ExecutionException {
        FreeStyleProject project = createFreeStyleProject();
        if(System.getProperty("os.name").contains("Windows")) {
            project.getBuildersList().add(new BatchFile("echo hello"));
        } else {
            project.getBuildersList().add(new Shell("echo hello"));
        }

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        System.out.println(build.getDisplayName()+" completed");

        String s = FileUtils.readFileToString(build.getLogFile());
        assertTrue(s,s.contains("echo hello"));
    }
}
