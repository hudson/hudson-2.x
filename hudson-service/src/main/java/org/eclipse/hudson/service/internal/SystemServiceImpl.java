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

package org.eclipse.hudson.service.internal;

import org.eclipse.hudson.service.SecurityService;
import org.eclipse.hudson.service.ServiceRuntimeException;
import org.eclipse.hudson.service.SystemService;
import org.eclipse.hudson.utils.io.FileUtil;

import hudson.XmlFile;
import hudson.init.InitMilestone;
import hudson.lifecycle.RestartNotSupportedException;
import hudson.model.Hudson;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link SystemService} implementation
 *
 * @author plynch
 * @since 2.1.0
 */
@Named
@Singleton
public class SystemServiceImpl
    extends ServiceSupport
    implements SystemService
{
    private final SecurityService securityService;

    @Inject
    SystemServiceImpl(final SecurityService securityService) {
        this.securityService = checkNotNull(securityService);
    }

    public File getInstallationDirectory() {
        //securityService.checkPermission(Hudson.ADMINISTER);
        File dir;
        try {
            // verbose to help pinpoint any NPE at runtime
            ProtectionDomain pd = Hudson.class.getProtectionDomain();
            CodeSource cs = pd.getCodeSource();
            URL url = cs.getLocation();
            String path = url.getPath();
            dir = new File(path);
            // Jar containing Launcher is expected in <install>/lib/some.jar (so .jar file - lib dir - should get us the install dir)
            dir = dir.getParentFile().getParentFile();
            dir = FileUtil.canonicalize(dir);
        }
        catch (NullPointerException e) {
            throw new IllegalStateException("Could not reliably determine the installation directory", e);
        }
        return dir;
    }

    public File getLogDirectory() {
        //securityService.checkPermission(Hudson.ADMINISTER);
        // From the installation directory these are located at var/log
        File file = new File(getInstallationDirectory(), "var/log");
        file = FileUtil.canonicalize(file);
        return file;
    }

    public File getWorkingDirectory() {
        //securityService.checkPermission(Hudson.ADMINISTER);
        return getHudson().getRootDir();
    }

    public XmlFile getConfigFile()
    {
        securityService.checkPermission(Hudson.ADMINISTER);
        // Hudson.getConfigFile() is not public, so we have to duplicate some muck here
        File f = new File(getWorkingDirectory(), "config.xml");
        return new XmlFile(Hudson.XSTREAM, f);
    }

    public String getUrl()
    {
        String url = getHudson().getRootUrl();

        if (url == null) {
            log.warn("Underlying Hudson root url is null; using DEFAULT_URL");
            url = DEFAULT_URL;
        }
        else if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }

    public String getVersion()
    {
        return Hudson.getVersion().toString();
    }

    public InitMilestone getInitLevel()
    {
        return getHudson().getInitLevel();
    }

    public boolean isQuietingDown()
    {
        return getHudson().isQuietingDown();
    }

    public boolean isTerminating()
    {
        return getHudson().isTerminating();
    }

    public String getSystemMessage()
    {
        return getHudson().getSystemMessage();
    }

    public void doQuietDown()
    {
        //securityService.checkPermission(Hudson.ADMINISTER);
        try{getHudson().doQuietDown();}catch(final IOException e){}
    }

    public void doQuietDown(boolean toggle)
    {
        //securityService.checkPermission(Hudson.ADMINISTER);
        if (toggle)
        {
            log.debug("Quieting down");
            doQuietDown();
        }
        else
        {
            log.debug("Canceling quiet down");
            doCancelQuietDown();
        }
    }

    public void doCancelQuietDown()
    {
        //securityService.checkPermission(Hudson.ADMINISTER);
        getHudson().doCancelQuietDown();
    }

    public void doReload()
    {
        //securityService.checkPermission(Hudson.ADMINISTER);
        log.debug("Reloading configuration");
        try
        {
            getHudson().doReload();
        }
        catch (IOException ex)
        {
            throw new ServiceRuntimeException("Could not reload.", ex);
        }
    }

    public void doRestart(boolean safely)
    {
        //securityService.checkPermission(Hudson.ADMINISTER);
        try
        {
            if (safely)
            {
                log.debug("Restarting (safely)");
                getHudson().safeRestart();

            }
            else
            {
                log.debug("Restarting");
                getHudson().restart();
            }
        }
        catch (RestartNotSupportedException ex)
        {
            throw new ServiceRuntimeException("Restart not supported", ex);
        }
    }

    public void doRestart()
    {
        //securityService.checkPermission(Hudson.ADMINISTER);
        log.debug("Restarting");
        try
        {
            getHudson().safeRestart();
        }
        catch (RestartNotSupportedException ex)
        {
            throw new ServiceRuntimeException("Restart not supported", ex);
        }
    }

    public void doRestartSafely()
    {
        //securityService.checkPermission(Hudson.ADMINISTER);
        log.debug("Restarting (safely)");
        try
        {
            getHudson().restart();
        }
        catch (RestartNotSupportedException ex)
        {
            throw new ServiceRuntimeException("Restart not supported", ex);
        }
    }
}
