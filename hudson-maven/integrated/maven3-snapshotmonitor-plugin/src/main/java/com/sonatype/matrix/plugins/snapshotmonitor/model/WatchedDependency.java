/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.plugins.snapshotmonitor.model;

import com.sonatype.matrix.maven.model.MavenCoordinatesDTO;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Container for dependency and last-modified state.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@XStreamAlias("dependency")
public class WatchedDependency
    extends MavenCoordinatesDTO
{
    @XStreamAsAttribute
    private long lastModified;

    public WatchedDependency(final MavenCoordinatesDTO dep) {
        checkNotNull(dep);
        setGroupId(dep.getGroupId());
        setArtifactId(dep.getArtifactId());
        setClassifier(dep.getClassifier());
        setType(dep.getType());
        setVersion(dep.getVersion());
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(final long lastModified) {
        this.lastModified = lastModified;
    }
}