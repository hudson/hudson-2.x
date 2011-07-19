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

package org.eclipse.hudson.rest.plugin.components;

import org.eclipse.hudson.rest.api.internal.AcegiSecurityExceptionMapper;
import org.eclipse.hudson.rest.api.internal.FaultExceptionMapper;
import org.eclipse.hudson.rest.api.internal.GenericExceptionMapper;
import org.eclipse.hudson.rest.api.internal.HandshakeResource;
import org.eclipse.hudson.rest.api.internal.NotFoundExceptionMapper;
import org.eclipse.hudson.rest.api.internal.WebApplicationExceptionMapper;
import org.eclipse.hudson.rest.api.status.StatusResource;
import org.eclipse.hudson.rest.common.JacksonProvider;
import org.eclipse.hudson.rest.plugin.RestComponentProvider;

import javax.inject.Named;
import javax.inject.Singleton;


/**
 * Provides the core components required by the REST subsystem.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class CoreComponents
    extends RestComponentProvider
{
    @Override
    public Class<?>[] getClasses() {
        return new Class[] {
            JacksonProvider.class,
            FaultExceptionMapper.class,
            NotFoundExceptionMapper.class,
            AcegiSecurityExceptionMapper.class,
            WebApplicationExceptionMapper.class,
            GenericExceptionMapper.class,

            // These are not really core, but specific to the handshake
            HandshakeResource.class,
            StatusResource.class,
        };
    }
}
