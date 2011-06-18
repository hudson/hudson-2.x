 
package hudson.maven;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.eclipse.hudson.legacy.maven.plugin.MavenModule;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.Maven3Builder
 */
public class MavenBuild extends org.eclipse.hudson.legacy.maven.plugin.MavenBuild {

	public MavenBuild(MavenModule job) throws IOException {
		super(job);
	}
    
	public MavenBuild(MavenModule job, Calendar timestamp) {
        super(job, timestamp);
    }

    public MavenBuild(MavenModule project, File buildDir) throws IOException {
        super(project, buildDir);
    }

}
