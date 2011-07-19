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

import hudson.FilePath;
import hudson.util.ClasspathBuilder;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.apache.commons.io.FilenameUtils.separatorsToWindows;

/**
 * Helper to normalize path arguments for slaves.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class PathNormalizer
{
    public static enum Platform
    {
        UNIX,
        WINDOWS
    }

    private final Platform platform;

    public PathNormalizer(final Platform platform) {
        this.platform = checkNotNull(platform);
    }

    public String normalize(final String path) {
        assert path != null;
        switch (platform) {
            case UNIX:
                return separatorsToUnix(path);
            case WINDOWS:
                return separatorsToWindows(path);
            default:
                throw new Error();
        }
    }

    public FilePath normalize(final FilePath path) {
        assert path != null;
        String tmp = normalize(path.getRemote());
        return new FilePath(path.getChannel(), tmp);
    }

    public ClasspathBuilder normalize(final ClasspathBuilder cp) {
        assert cp != null;
        ClasspathBuilder target = new ClasspathBuilder();
        for (String path : cp) {
            target.add(normalize(path));
        }
        return target;
    }
}
