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

package org.hudsonci.service;

import com.google.inject.ImplementedBy;
import hudson.XmlFile;
import hudson.init.InitMilestone;
import hudson.model.Hudson;

import java.io.File;

import org.hudsonci.service.internal.SystemServiceImpl;

/**
 * General system access.
 *
 * <p>All operations involving restarting, quiet down, or reloading the system require {@link Hudson#ADMINISTER} permission.
 *
 * @author plynch
 * @since 2.1.0
 */
@ImplementedBy(SystemServiceImpl.class)
public interface SystemService
{
    String DEFAULT_URL = "http://localhost:8082";

    /**
     * The installation location.
     *
     * @throws IllegalStateException when the installation directory cannot be reliably determined
     */
    File getInstallationDirectory();

    /**
     * Get the working directory of the server
     */
    // TODO: as of 1.1 this is the "state" directory, maybe rename
    File getWorkingDirectory();

    /**
     * The default location where the server will store log files.
     *
     * @throws IllegalStateException if the log directory cannot be reliably determined
     */
    File getLogDirectory();

    /**
     * Return the {@link XmlFile} representation of the system config file
     *
     * The current thread requires {@link Hudson#ADMINISTER} permission to get the file.
     *
     * @return XmlFile representation of the system config file,
     * @throws ServiceRuntimeException if there is an unexpected problem accessing the config file.
     */
    XmlFile getConfigFile();

    String getUrl();

    String getVersion();

    InitMilestone getInitLevel();

    String getSystemMessage();

    // FIXME: Drop do* prefix on these methods.  do* is specific to stapler crap and should be dropped from the service API

    boolean isQuietingDown();

    void doQuietDown(boolean toggle);

    void doQuietDown();

    void doCancelQuietDown();

    void doReload();

    boolean isTerminating();

    void doRestart();

    void doRestart(boolean safe);

    void doRestartSafely();
}
