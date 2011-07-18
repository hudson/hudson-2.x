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
