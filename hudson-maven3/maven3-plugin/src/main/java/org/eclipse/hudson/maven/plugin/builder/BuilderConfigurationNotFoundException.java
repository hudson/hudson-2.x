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

package org.eclipse.hudson.maven.plugin.builder;

import org.eclipse.hudson.service.NotFoundException;

/**
 * Thrown when a requested builder configuration is not found.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class BuilderConfigurationNotFoundException
    extends NotFoundException
{
    public BuilderConfigurationNotFoundException(final String projectName, final int index) {
        super(String.format("No such builder configuration for project '%s' at index %s", projectName, index));
    }
}
