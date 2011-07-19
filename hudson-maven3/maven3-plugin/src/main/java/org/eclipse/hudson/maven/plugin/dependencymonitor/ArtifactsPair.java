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

package org.eclipse.hudson.maven.plugin.dependencymonitor;

import com.google.common.collect.Sets;
import org.eclipse.hudson.maven.model.MavenCoordinatesDTO;

import java.util.Arrays;
import java.util.Collection;

/**
 * Container for the pair of produced and consumed artifacts for a project or build.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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
