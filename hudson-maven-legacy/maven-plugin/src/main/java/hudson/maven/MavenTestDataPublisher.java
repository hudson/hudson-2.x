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
