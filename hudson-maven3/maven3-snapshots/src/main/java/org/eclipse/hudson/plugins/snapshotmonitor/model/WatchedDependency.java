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

package org.eclipse.hudson.plugins.snapshotmonitor.model;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Container for dependency and last-modified state.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
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
