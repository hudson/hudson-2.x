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

import hudson.model.AbstractProject;

/**
 * Indicates that an operation on a sub-class of {@link AbstractProject} is not supported by a project type.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
public class UnsupportedProjectException
    extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private final AbstractProject project;

    public UnsupportedProjectException( final AbstractProject project )
    {
        super(String.format( "Unsupported project '%s' of type %s.", project.getFullDisplayName(), project.getClass()));
        this.project = project;
    }
    
    public UnsupportedProjectException( final AbstractProject project, final String moreDetail )
    {
        super(String.format( "Unsupported project '%s' of type %s : %s.", project.getFullDisplayName(), project.getClass(), moreDetail));
        this.project = project;
    }

    public AbstractProject getProject()
    {
        return project;
    }
}
