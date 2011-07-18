/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

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
