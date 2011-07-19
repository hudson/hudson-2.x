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
*    Kohsuke Kawaguchi, id:cactusman
 *     
 *
 *******************************************************************************/ 

package hudson.maven;

import org.eclipse.hudson.legacy.maven.plugin.*;

import java.io.IOException;

/**
 * Exists solely for backward compatibility
 * 
 * @author Winston Prakash
 * @see org.eclipse.hudson.legacy.maven.plugin.MavenModule
 */
public final class MavenModule extends org.eclipse.hudson.legacy.maven.plugin.MavenModule {

    MavenModule(MavenModuleSet parent, PomInfo pom, int firstBuildNumber) throws IOException {
        super(parent, pom, firstBuildNumber);

    }
}
