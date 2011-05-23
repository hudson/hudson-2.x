/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.plugins.snapshotmonitor;

import com.sonatype.matrix.ui.JellyAccessible;
import com.sonatype.matrix.maven.model.MavenCoordinatesDTO;
import hudson.model.Cause;

import java.util.Collection;

/**
 * Cause when an external SNAPSHOT dependency change triggers a build.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.2
 */
public class DependenciesChangedCause
    extends Cause
{
    private final Collection<MavenCoordinatesDTO> dependencies;

    public DependenciesChangedCause(final Collection<MavenCoordinatesDTO> dependencies) {
        assert dependencies != null;
        this.dependencies = dependencies;
    }

    @JellyAccessible
    public Collection<MavenCoordinatesDTO> getDependencies() {
        return dependencies;
    }

    @Override
    public String getShortDescription() {
        // TODO: Use localizer
        return "External SNAPSHOT dependency change";
    }
}