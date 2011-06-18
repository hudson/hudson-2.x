package hudson.maven;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.AbstractMavenProject
 */
public abstract class AbstractMavenProject<P extends AbstractProject<P, R>, R extends AbstractBuild<P, R>>
		extends
		org.eclipse.hudson.legacy.maven.plugin.AbstractMavenProject<P, R> {

	protected AbstractMavenProject(ItemGroup parent, String name) {
		super(parent, name);
	}
}
