/*******************************************************************************
 *
 * Copyright (c) 2010-2011 Sonatype, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *     
 *
 *******************************************************************************/ 

package org.eclipse.hudson.plugins.snapshotmonitor;

import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;

import java.io.IOException;

import org.eclipse.hudson.plugins.snapshotmonitor.model.WatchedDependency;

/**
 * Checks Maven metadata for a {@link WatchedDependency}.
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public interface MetadataChecker
{
    String getPath(MavenCoordinatesDTO artifact);

    long check(WatchedDependency dependency) throws IOException;
}
