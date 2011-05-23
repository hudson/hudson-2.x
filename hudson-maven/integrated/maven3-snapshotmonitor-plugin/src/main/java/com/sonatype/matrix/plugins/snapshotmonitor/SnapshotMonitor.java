/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.plugins.snapshotmonitor;

import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.model.MavenCoordinatesDTO;
import com.sonatype.matrix.plugins.snapshotmonitor.internal.SnapshotMonitorImpl;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import java.io.IOException;
import java.util.Collection;

// FIXME: Rename, external repository monitory or something?

/**
 * ???
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.2
 */
@ImplementedBy(SnapshotMonitorImpl.class)
public interface SnapshotMonitor
{
    boolean isConfigured();

    void update(AbstractBuild build, Collection<MavenCoordinatesDTO> dependencies) throws IOException;

    void check(AbstractProject project) throws IOException;
}