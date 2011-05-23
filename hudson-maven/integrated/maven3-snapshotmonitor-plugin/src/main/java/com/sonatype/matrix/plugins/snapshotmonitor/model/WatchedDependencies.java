/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.plugins.snapshotmonitor.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import java.util.Collection;

/**
 * Container for the watched dependencies of a project.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@XStreamAlias("watched-dependencies")
@XStreamInclude(WatchedDependency.class)
public class WatchedDependencies
{
    @XStreamImplicit
    private Collection<WatchedDependency> dependencies;

    public Collection<WatchedDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(final Collection<WatchedDependency> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public String toString() {
        return "WatchedDependencies{" +
            "dependencies=" + dependencies +
            '}';
    }
}