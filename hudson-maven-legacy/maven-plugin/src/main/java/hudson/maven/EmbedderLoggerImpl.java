
package hudson.maven;

import hudson.model.TaskListener;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.EmbedderLoggerImpl
 */
public final class EmbedderLoggerImpl extends org.eclipse.hudson.legacy.maven.plugin.EmbedderLoggerImpl {

	public EmbedderLoggerImpl(TaskListener listener, int threshold) {
		super(listener, threshold);
	}
    
}
