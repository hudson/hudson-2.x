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

import org.eclipse.hudson.rest.api.admin.AdminResource;
import org.eclipse.hudson.rest.api.build.BuildResource;
import org.eclipse.hudson.rest.api.node.NodeResource;
import org.eclipse.hudson.rest.api.project.ProjectResource;
import org.eclipse.hudson.rest.api.project.ProjectsResource;
import org.eclipse.hudson.rest.api.queue.QueueResource;
import org.eclipse.hudson.rest.api.user.UserResource;
import org.eclipse.hudson.rest.plugin.RestComponentProvider;

import javax.inject.Named;
import javax.inject.Singleton;


/**
 * Provides Hudson-specific REST components.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class HudsonComponents
    extends RestComponentProvider
{
    @Override
    public Class<?>[] getClasses() {
        return new Class[] {
            AdminResource.class,
            BuildResource.class,
            ProjectsResource.class,
            NodeResource.class,
            QueueResource.class,
            UserResource.class,
            
            //
            // WORK AROUND: This is added to test by UUID reference bits, should eventually only allow one method.
            //
            ProjectResource.class,
        };
    }
}
