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
