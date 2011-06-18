 
package hudson.maven;

import hudson.model.BuildListener;

import java.util.List;
import java.util.Map;

/**
 * Exists solely for backward compatibility
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.AbstractMavenBuilder
 */
public abstract class AbstractMavenBuilder extends org.eclipse.hudson.legacy.maven.plugin.AbstractMavenBuilder {

	protected AbstractMavenBuilder(BuildListener listener, List<String> goals,
			Map<String, String> systemProps) {
		super(listener, goals, systemProps);
	}
      
}
