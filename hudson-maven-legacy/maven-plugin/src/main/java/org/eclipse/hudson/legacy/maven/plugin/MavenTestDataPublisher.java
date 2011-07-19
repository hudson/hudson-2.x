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
*    Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.legacy.maven.plugin;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Saveable;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.junit.TestDataPublisher;
import hudson.tasks.junit.TestResultAction.Data;
import hudson.util.DescribableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.eclipse.hudson.legacy.maven.plugin.reporters.SurefireReport;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Augments {@link SurefireReport} by executing {@link TestDataPublisher}s.
 * @since 1.320
 */
public class MavenTestDataPublisher extends Recorder {

	private final DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>> testDataPublishers;

	public MavenTestDataPublisher(
			DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>> testDataPublishers) {
		super();
		this.testDataPublishers = testDataPublishers;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.STEP;
	}

	public boolean perform(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {

		SurefireReport report = build.getAction(SurefireReport.class);
		if (report == null) {
			return true;
		}
		
		List<Data> data = new ArrayList<Data>();
		if (testDataPublishers != null) {
			for (TestDataPublisher tdp : testDataPublishers) {
				Data d = tdp.getTestData(build, launcher, listener, report.getResult());
				if (d != null) {
					data.add(d);
				}
			}
		}
		
		report.setData(data);

		return true;
	}

	public DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>> getTestDataPublishers() {
		return testDataPublishers;
	}
	
	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		@Override
		public String getDisplayName() {
			return "Additional test report features";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return MavenModuleSet.class.isAssignableFrom(jobType) && !TestDataPublisher.all().isEmpty();
		}
		
		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>> testDataPublishers
                    = new DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>>(Saveable.NOOP);
            try {
                testDataPublishers.rebuild(req, formData, TestDataPublisher.all());
            } catch (IOException e) {
                throw new FormException(e,null);
            }

            return new MavenTestDataPublisher(testDataPublishers);
		}
		
	}

}
