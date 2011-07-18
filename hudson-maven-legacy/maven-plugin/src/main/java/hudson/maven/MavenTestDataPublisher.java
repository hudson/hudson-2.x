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

package hudson.maven;

import hudson.model.Descriptor;
import hudson.tasks.junit.TestDataPublisher;
import hudson.util.DescribableList;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.MavenReportInfo
 */
public class MavenTestDataPublisher extends
		org.eclipse.hudson.legacy.maven.plugin.MavenTestDataPublisher {

	public MavenTestDataPublisher(
			DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>> testDataPublishers) {
		super(testDataPublishers);
	}

	public static class DescriptorImpl
			extends
			org.eclipse.hudson.legacy.maven.plugin.MavenTestDataPublisher.DescriptorImpl {

	}

}
