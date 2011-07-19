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

package org.eclipse.hudson.utils.tasks;

import hudson.FilePath;
import hudson.Functions;
import hudson.Util;
import hudson.remoting.VirtualChannel;
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
                Util.chmod(file.getAbsoluteFile(), mode);
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
