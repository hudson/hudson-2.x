/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.maven;

import hudson.AbortException;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.MavenUtil
 */
public class MavenUtil extends org.eclipse.hudson.legacy.maven.plugin.MavenUtil {

	public static MavenEmbedder createEmbedder(TaskListener listener,
			String profiles) throws MavenEmbedderException, IOException {
		return org.eclipse.hudson.legacy.maven.plugin.MavenUtil.createEmbedder(
				listener, profiles);
	}

	public static MavenEmbedder createEmbedder(TaskListener listener,
			AbstractProject<?, ?> project, String profiles)
			throws MavenEmbedderException, IOException, InterruptedException {
		return org.eclipse.hudson.legacy.maven.plugin.MavenUtil.createEmbedder(listener,
				  project, profiles);
	}

	public static MavenEmbedder createEmbedder(TaskListener listener,
			AbstractBuild<?, ?> build) throws MavenEmbedderException,
			IOException, InterruptedException {
		return org.eclipse.hudson.legacy.maven.plugin.MavenUtil.createEmbedder(
				listener, build);
	}

	public static MavenEmbedder createEmbedder(TaskListener listener,
			File mavenHome, String profiles) throws MavenEmbedderException,
			IOException {
		return org.eclipse.hudson.legacy.maven.plugin.MavenUtil.createEmbedder(listener,
				 mavenHome,  profiles);
	}

	public static MavenEmbedder createEmbedder(TaskListener listener,
			File mavenHome, String profiles, Properties systemProperties)
			throws MavenEmbedderException, IOException {
		return org.eclipse.hudson.legacy.maven.plugin.MavenUtil.createEmbedder( listener,
				 mavenHome,  profiles,  systemProperties);
	}

	public static MavenEmbedder createEmbedder(TaskListener listener,
			File mavenHome, String profiles, Properties systemProperties,
			String privateRepository) throws MavenEmbedderException,
			IOException {
		return org.eclipse.hudson.legacy.maven.plugin.MavenUtil.createEmbedder( listener,
				 mavenHome,  profiles,  systemProperties,
				 privateRepository);
	}

	public static MavenEmbedder createEmbedder(
			MavenEmbedderRequest mavenEmbedderRequest)
			throws MavenEmbedderException, IOException {
		return org.eclipse.hudson.legacy.maven.plugin.MavenUtil.createEmbedder(
				 mavenEmbedderRequest);
	}

	public static void resolveModules(MavenEmbedder embedder,
			MavenProject project, String rel,
			Map<MavenProject, String> relativePathInfo, BuildListener listener,
			boolean nonRecursive) throws ProjectBuildingException,
			AbortException, MavenEmbedderException {
		org.eclipse.hudson.legacy.maven.plugin.MavenUtil.resolveModules(
				embedder, project, rel, relativePathInfo, listener,
				nonRecursive);
	}

	public static boolean maven3orLater(String mavenVersion) {
		return org.eclipse.hudson.legacy.maven.plugin.MavenUtil
				.maven3orLater(mavenVersion);
	}

}
