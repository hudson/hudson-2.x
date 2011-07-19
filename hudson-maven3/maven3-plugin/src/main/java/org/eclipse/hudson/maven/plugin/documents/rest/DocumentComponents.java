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

package org.eclipse.hudson.maven.plugin.documents.rest;

import org.eclipse.hudson.rest.plugin.RestComponentProvider;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Configures REST components for the documents subsystem.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Named
@Singleton
public class DocumentComponents
    extends RestComponentProvider
{
    @Override
    public Class<?>[] getClasses() {
        return new Class[] {
            DocumentResource.class,
            DocumentNotFoundExceptionMapper.class,
            DuplicateDocumentExceptionMapper.class
        };
    }
}
