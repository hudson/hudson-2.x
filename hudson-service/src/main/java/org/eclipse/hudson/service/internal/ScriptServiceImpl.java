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

import hudson.model.Hudson;
import hudson.model.Hudson.MasterComputer;
import hudson.remoting.VirtualChannel;
import hudson.util.RemotingDiagnostics;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.hudson.service.ScriptService;
import org.eclipse.hudson.service.SecurityService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link ScriptService}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class ScriptServiceImpl
    extends ServiceSupport
    implements ScriptService
{
    private final SecurityService security;

    @Inject
    public ScriptServiceImpl(final SecurityService security) {
        this.security = checkNotNull(security);
    }

    /**
     * {@inheritDoc}
     *
     * @see RemotingDiagnostics#executeGroovy(String, VirtualChannel)
     */
    public String execute(final String script) throws Exception {
        checkNotNull(script);
        security.checkPermission(Hudson.ADMINISTER);
        log.debug("Executing script on master: {}", script);
        return RemotingDiagnostics.executeGroovy(script, MasterComputer.localChannel);
    }
}
