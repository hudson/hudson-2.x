 
package hudson.maven;

import hudson.model.Hudson;
import hudson.model.ItemGroup;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.MavenModuleSet
 */
public final class MavenModuleSet extends  org.eclipse.hudson.legacy.maven.plugin.MavenModuleSet{

	public MavenModuleSet(String name) {
        this(Hudson.getInstance(),name);
    }
	
	public MavenModuleSet(ItemGroup parent, String name) {
		super(parent, name);
		// TODO Auto-generated constructor stub
	}
}
