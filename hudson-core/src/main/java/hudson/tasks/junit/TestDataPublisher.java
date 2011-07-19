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
*    Tom Huybrechts, Yahoo!, Inc.
 *     
 *
 *******************************************************************************/ 

package hudson.tasks.junit;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.Launcher;
import hudson.model.*;

import java.io.IOException;

/**
 * Contributes {@link TestAction}s to test results.
 *
 * This enables plugins to annotate test results and provide richer UI, such as letting users
 * claim test failures, allowing people to file bugs, or more generally, additional actions, views, etc.
 *
 * <p>
 * To register your implementation, put {@link Extension} on your descriptor implementation. 
 *
 * @since 1.320
 */
public abstract class TestDataPublisher extends AbstractDescribableImpl<TestDataPublisher> implements ExtensionPoint {

    /**
     * Called after test results are collected by Hudson, to create a resolver for {@link TestAction}s.
     *
     * @return
     *      can be null to indicate that there's nothing to contribute for this test result.
     */
	public abstract TestResultAction.Data getTestData(
			AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener, TestResult testResult) throws IOException, InterruptedException;

	public static DescriptorExtensionList<TestDataPublisher, Descriptor<TestDataPublisher>> all() {
		return Hudson.getInstance().<TestDataPublisher, Descriptor<TestDataPublisher>>getDescriptorList(TestDataPublisher.class);
	}

}
