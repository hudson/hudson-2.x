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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base Hudson Service from which all service implementations should extend.
 *
 * <p>Common {@literal preconditions} of service implementation methods
 * include:
 *
 * <ul>
 * <li>throw a {@link NullPointerException} if a null object
 * reference is passed in any parameter.
 * <li>throw an {@link org.acegisecurity.AccessDeniedException} if the current thread context does not hold a required authority
 * to perform an operation
 * </ul>
 *
 * @author plynch
 * @since 2.1.0
 */
public abstract class ServiceSupport
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Hudson hudson;

    @Inject
    public void setHudson(final Hudson hudson) {
        this.hudson = checkNotNull(hudson);
    }

    protected Hudson getHudson()
    {
        return hudson;
    }
}
