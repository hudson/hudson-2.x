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

import com.google.inject.ImplementedBy;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.hudson.plugins.snapshotmonitor.internal.SnapshotMonitorImpl;

// FIXME: Rename, external repository monitory or something?

/**
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@ImplementedBy(SnapshotMonitorImpl.class)
public interface SnapshotMonitor
{
    boolean isConfigured();

    void update(AbstractBuild build, Collection<MavenCoordinatesDTO> dependencies) throws IOException;

    void check(AbstractProject project) throws IOException;
}
