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
