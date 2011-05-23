/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.dependencymonitor;

import com.google.common.collect.Sets;
import com.sonatype.matrix.maven.model.MavenCoordinatesDTO;

import java.util.Arrays;
import java.util.Collection;

/**
 * Container for the pair of produced and consumed artifacts for a project or build.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public class ArtifactsPair
{
    public final Collection<MavenCoordinatesDTO> produced;

    public final Collection<MavenCoordinatesDTO> consumed;

    // FIXME: May actually need to bring immutability back to these guys, or make ^^^ private and expose access which returns immutables or something

    public ArtifactsPair(final Collection<MavenCoordinatesDTO> produced, final Collection<MavenCoordinatesDTO> consumed) {
        // either may be null
        this.produced = produced != null ? produced : Sets.<MavenCoordinatesDTO>newHashSet();
        this.consumed = consumed != null ? consumed : Sets.<MavenCoordinatesDTO>newHashSet();
    }

    public ArtifactsPair() {
        this(null, null);
    }

    public boolean isEmpty() {
        return produced.isEmpty() && consumed.isEmpty();
    }

    public ArtifactsPair withProduced(final MavenCoordinatesDTO... artifacts) {
        if (artifacts != null) {
            produced.addAll(Arrays.asList(artifacts));
        }
        return this;
    }

    public ArtifactsPair withConsumed(final MavenCoordinatesDTO... artifacts) {
        if (artifacts != null) {
            consumed.addAll(Arrays.asList(artifacts));
        }
        return this;
    }
}
