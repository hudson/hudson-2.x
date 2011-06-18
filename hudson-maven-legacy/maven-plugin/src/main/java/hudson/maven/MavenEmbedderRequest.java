package hudson.maven;

import hudson.model.TaskListener;

import java.io.File;
import java.util.Properties;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.Maven3Builder
 */
public class MavenEmbedderRequest extends
		org.eclipse.hudson.legacy.maven.plugin.MavenEmbedderRequest {

	public MavenEmbedderRequest(TaskListener listener, File mavenHome,
			String profiles, Properties systemProperties,
			String privateRepository, File alternateSettings) {
		super(listener, mavenHome, profiles, systemProperties, privateRepository,
				alternateSettings);
	}

}
