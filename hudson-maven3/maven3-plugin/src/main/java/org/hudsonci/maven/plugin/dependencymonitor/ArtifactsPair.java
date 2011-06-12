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

package org.hudsonci.maven.plugin.dependencymonitor;

import com.google.common.collect.Sets;
import org.hudsonci.maven.model.MavenCoordinatesDTO;

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
