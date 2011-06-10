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

package org.hudsonci.utils.tasks;

import hudson.FilePath;
import hudson.Functions;
import hudson.os.PosixAPI;
import hudson.remoting.VirtualChannel;
import hudson.util.jna.GNUCLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Change file(s) permissions on remote node.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class Chmod
    implements FilePath.FileCallable<Void>
{
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(Chmod.class);

    private final int mode;

    public Chmod(final int mode) {
        this.mode = mode;
    }

    public Void invoke(final File file, final VirtualChannel channel) throws IOException {
        if (!Functions.isWindows()) {
            process(file);
        }
        return null;
    }

    private void process(final File file) {
        assert file != null;

        if (file.isFile()) {
            if (Functions.isMustangOrAbove()) {
                if (!file.setExecutable(true, false)) {
                    log.error("Failed to chmod: {}", file);
                }
            }
            else {
                try {
                    GNUCLibrary.LIBC.chmod(file.getAbsolutePath(), mode);
                }
                catch (LinkageError e) {
                    // if JNA is unavailable, fall back
                    PosixAPI.get().chmod(file.getAbsolutePath(), mode);
                }
            }
        }
        else {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    process(child);
                }
            }
        }
    }
}
