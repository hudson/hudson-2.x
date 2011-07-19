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

import hudson.model.AbstractProject;

/**
 * Thrown when an {@link AbstractProject} is needed to perform an operation, but could not be located.
 *
 * @author plynch
 * @since 2.1.0
 */
public class ProjectNotFoundException
    extends NotFoundException
{
    public ProjectNotFoundException(String message)
    {
        super(message);
    }

    public ProjectNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
