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

package org.hudsonci.maven.plugin.builder.internal;

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
