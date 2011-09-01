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

package org.eclipse.hudson.service;

import com.google.inject.ImplementedBy;
import hudson.XmlFile;
import hudson.init.InitMilestone;
import hudson.model.Hudson;

import java.io.File;

import org.eclipse.hudson.service.internal.SystemServiceImpl;

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

    // FIXME: Drop do* prefix on these methods.  do* is specific to stapler and should be dropped from the service API

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
