package hudson.maven;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.Maven3Builder
 */
public class MavenBuildInformation extends
		org.eclipse.hudson.legacy.maven.plugin.MavenBuildInformation {

	private static final long serialVersionUID = 1L;

	public MavenBuildInformation(String mavenVersion) {
		super(mavenVersion);
	}

}
