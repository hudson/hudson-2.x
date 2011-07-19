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

package org.eclipse.hudson.maven.plugin.builder.internal;

import org.eclipse.hudson.maven.plugin.builder.internal.PathNormalizer;
import org.junit.Test;

import static org.eclipse.hudson.maven.plugin.builder.internal.PathNormalizer.Platform.UNIX;
import static org.eclipse.hudson.maven.plugin.builder.internal.PathNormalizer.Platform.WINDOWS;
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
