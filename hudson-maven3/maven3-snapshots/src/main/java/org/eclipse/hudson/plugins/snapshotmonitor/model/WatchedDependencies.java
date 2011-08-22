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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import java.util.Collection;

/**
 * Container for the watched dependencies of a project.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@XStreamAlias("watched-dependencies")
@XStreamInclude(WatchedDependency.class)
public class WatchedDependencies
{
    @XStreamImplicit(itemFieldName="dependency")
    private Collection<WatchedDependency> dependencies;

    public Collection<WatchedDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(final Collection<WatchedDependency> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public String toString() {
        return "WatchedDependencies{" + dependencies + '}';
    }
}
