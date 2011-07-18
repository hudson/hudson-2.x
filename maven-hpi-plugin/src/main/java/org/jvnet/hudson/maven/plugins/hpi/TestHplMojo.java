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
 *
 *******************************************************************************/ 

package org.jvnet.hudson.maven.plugins.hpi;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

/**
 * Generate .hpl file in the test class directory so that test harness can locate the plugin.
 *
 * @goal test-hpl
 * @requiresDependencyResolution test
 * @author Kohsuke Kawaguchi
 */
public class TestHplMojo extends HplMojo {

    public TestHplMojo() {
        includeTestScope = true;
    }

    /**
     * Generates the hpl file in a known location.
     */
    @Override
    protected File computeHplFile() throws MojoExecutionException {
        File testDir = new File(project.getBuild().getTestOutputDirectory());
        testDir.mkdirs();
        return new File(testDir,"the.hpl");
    }
}
