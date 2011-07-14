package hudson.maven;

import org.eclipse.hudson.legacy.maven.plugin.*;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.MavenBuildProxy
 */
public interface MavenBuildProxy extends org.eclipse.hudson.legacy.maven.plugin.MavenBuildProxy {

    public interface BuildCallable<V, T extends Throwable> extends org.eclipse.hudson.legacy.maven.plugin.MavenBuildProxy.BuildCallable<V, T> {
    }
}
