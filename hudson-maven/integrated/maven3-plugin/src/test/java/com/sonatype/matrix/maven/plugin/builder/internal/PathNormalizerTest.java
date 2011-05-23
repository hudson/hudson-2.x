/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.builder.internal;

import org.junit.Test;

import static com.sonatype.matrix.maven.plugin.builder.internal.PathNormalizer.Platform.WINDOWS;
import static com.sonatype.matrix.maven.plugin.builder.internal.PathNormalizer.Platform.UNIX;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link PathNormalizer}.
 */
public class PathNormalizerTest
{
    @Test
    public void testStringPathToWindows() {
        PathNormalizer normalizer = new PathNormalizer(WINDOWS);
        String path = normalizer.normalize("/foo\\bar/baz");
        assertEquals("\\foo\\bar\\baz", path);
    }

    @Test
    public void testStringPathToUnix() {
        PathNormalizer normalizer = new PathNormalizer(UNIX);
        String path = normalizer.normalize("/foo\\bar/baz");
        assertEquals("/foo/bar/baz", path);
    }
}
