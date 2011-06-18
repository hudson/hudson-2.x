package hudson.maven;

import java.io.File;
import java.io.IOException;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.MavenModuleSetBuild
 */
public class MavenModuleSetBuild extends org.eclipse.hudson.legacy.maven.plugin.MavenModuleSetBuild {

	public MavenModuleSetBuild(MavenModuleSet job) throws IOException {
		super(job);
	}

	public MavenModuleSetBuild(MavenModuleSet project, File buildDir)
			throws IOException {
		super(project, buildDir);
	}

}
