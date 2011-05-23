/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder;

import com.sonatype.matrix.common.marshal.xref.XReference;
import com.sonatype.matrix.maven.model.state.BuildStateDTO;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import hudson.model.AbstractBuild;
import hudson.model.InvisibleAction;

import java.io.File;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Records the {@link BuildStateDTO} for a build.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@XStreamAlias("maven-build-record")
@XStreamInclude({BuildStateDTO.class, BuildStateRecord.StateReference.class})
public class BuildStateRecord
    extends InvisibleAction
{
    private final UUID id = UUID.randomUUID();

    private final AbstractBuild<?,?> build;

    private final StateReference state;

    public BuildStateRecord(final AbstractBuild<?, ?> build) {
        this.build = checkNotNull(build);
        this.state = new StateReference(new BuildStateDTO());
    }

    public UUID getId() {
        return id;
    }

    public AbstractBuild<?,?> getBuild() {
        return build;
    }

    public BuildStateDTO getState() {
        return state.get();
    }

    @XStreamAlias("maven-build-record-state-ref")
    public class StateReference
        extends XReference<BuildStateDTO>
    {
        // TODO: Need to resolve how the holder of the reference can be flipped to soft after we have saved once
        // TODO: ... data won't change after its saved and we want to allow the jvm to reclaim its memory when its gets low
        // TODO: ... reloading from disk if asked for again

        public StateReference(final BuildStateDTO state) {
            super(state);
        }

        @Override
        public String getPath() {
            return new File(getBuild().getRootDir(), String.format("maven-build-%s.xml", getId())).getAbsolutePath();
        }
    }
}