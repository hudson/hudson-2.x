/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

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
