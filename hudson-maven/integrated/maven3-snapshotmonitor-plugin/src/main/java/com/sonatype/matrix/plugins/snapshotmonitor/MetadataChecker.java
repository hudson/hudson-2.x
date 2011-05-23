/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.plugins.snapshotmonitor;

import com.sonatype.matrix.maven.model.MavenCoordinatesDTO;
import com.sonatype.matrix.plugins.snapshotmonitor.model.WatchedDependency;

import java.io.IOException;

/**
 * Checks Maven metadata for a {@link WatchedDependency}.
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.2
 */
public interface MetadataChecker
{
    String getPath(MavenCoordinatesDTO artifact);

    long check(WatchedDependency dependency) throws IOException;
}