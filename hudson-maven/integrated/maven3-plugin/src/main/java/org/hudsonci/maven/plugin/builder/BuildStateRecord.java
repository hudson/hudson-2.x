/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.plugin.builder;

import org.hudsonci.utils.marshal.xref.XReference;
import org.hudsonci.maven.model.state.BuildStateDTO;
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
 * @since 2.1.0
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

        public String getPath() {
            return new File(getBuild().getRootDir(), String.format("maven-build-%s.xml", getId())).getAbsolutePath();
        }
    }
}
