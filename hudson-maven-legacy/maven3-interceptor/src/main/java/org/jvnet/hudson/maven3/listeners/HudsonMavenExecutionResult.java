package org.jvnet.hudson.maven3.listeners;


import org.apache.maven.execution.MavenExecutionResult;

/**
 * Exists for backward compatibility
 * @author Winston Prakash
 * @see org.eclipse.hudson.maven.interceptor.listeners.HudsonMavenExecutionResult
 */
public class HudsonMavenExecutionResult
		extends
		org.eclipse.hudson.maven.interceptor.listeners.HudsonMavenExecutionResult {

	public static final long serialVersionUID = -2236073185655598257L;

	public HudsonMavenExecutionResult(MavenExecutionResult mavenExecutionResult) {
		super(mavenExecutionResult);
	}
}
