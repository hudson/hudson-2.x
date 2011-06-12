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

import org.hudsonci.maven.plugin.dependencymonitor.ArtifactsPair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link ArtifactsPair}.
 */
public class ArtifactsPairTest
{
    @Test
    public void ensureNonNull() {
        ArtifactsPair artifacts = new ArtifactsPair(null, null);
        assertNotNull(artifacts.produced);
        assertEquals(0, artifacts.produced.size());
        assertNotNull(artifacts.consumed);
        assertEquals(0, artifacts.consumed.size());
    }

    @Test
    public void ensureIsEmpty() {
        ArtifactsPair artifacts1 = new ArtifactsPair(null, null);
        assertTrue(artifacts1.isEmpty());

        ArtifactsPair artifacts2 = new ArtifactsPair();
        assertTrue(artifacts2.isEmpty());
    }
}
