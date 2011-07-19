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

package org.hudsonci.utils.test;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Test utilities.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class TestUtil
{
    private final Class owningClass;

    /**
     * The base-directory which tests should be run from.
     *
     * @see #initBaseDir()   This field is initialized from the return of this method on instance construction.
     */
    protected final File baseDir;

    /**
     * Instance logger which tests should use to produce tracing information.
     *
     * <p>
     * Unless you have a really good reason to, do not change this field from your sub-class.
     * And if you do, please document why you have done so.
     */
    protected final Logger log;

    public TestUtil(final Class owner) {
        if (null == owner) {
            throw new IllegalArgumentException("owner is required");
        }
        this.owningClass = owner;

        baseDir = initBaseDir();

        //
        // NOTE: Logging must be initialized after BASEDIR has been discovered, as it is used
        //       by the log4j logging-config properties to set the target/test.log file.
        //
        log = LoggerFactory.getLogger(owner);
    }

    public TestUtil(final Object owner) {
        this(owner.getClass());
    }

    public File getBaseDir() {
        return baseDir;
    }

    public Logger getLog() {
        return log;
    }

    /**
     * Determine the value of <tt>${basedir}</tt>, which should be the base directory of
     * the module which the concrete test class is defined in.
     *
     * <p>
     * If The system property <tt>basedir</tt> is already set, then that value is used,
     * otherwise we determine the value from the code-source of the containing concrete class
     * and set the <tt>basedir</tt> system property to that value.
     *
     * @see #baseDir    This field is always initialized to the value which this method returns.
     *
     * @return  The base directory of the module which contains the concrete test class.
     */
    protected final File initBaseDir() {
        File dir;

        // If ${basedir} is set, then honor it
        String tmp = System.getProperty("basedir");
        if (tmp != null) {
            dir = new File(tmp);
        }
        else {
            // Find the directory which this class (or really the sub-class of TestSupport) is defined in.
            String path = owningClass.getProtectionDomain().getCodeSource().getLocation().getFile();

            // We expect the file to be in target/test-classes, so go up 2 dirs
            dir = new File(path).getParentFile().getParentFile();

            // Set ${basedir} which is needed by logging to initialize
            System.setProperty("basedir", dir.getPath());
        }

        // System.err.println("Base Directory: " + dir);

        return dir;
    }

    /**
     * Resolve the given path to a file rooted to {@link #baseDir}.
     *
     * @param path  The path to resolve.
     * @return      The resolved file for the given path.
     */
    public final File resolveFile(final String path) {
        Preconditions.checkNotNull(path);

        File file = new File(path);

        // Complain if the file is already absolute... probably an error
        if (file.isAbsolute()) {
            log.warn("Given path is already absolute; nothing to resolve: {}", file);
        }
        else {
            file = new File(baseDir, path);
        }

        return file;
    }

    /**
     * Resolve the given path to a path rooted to {@link #baseDir}.
     *
     * @param path  The path to resolve.
     * @return      The resolved path for the given path.
     *
     * @see #resolveFile(String)
     */
    public final String resolvePath(final String path) {
        Preconditions.checkNotNull(path);

        return resolveFile(path).getPath();
    }
}
