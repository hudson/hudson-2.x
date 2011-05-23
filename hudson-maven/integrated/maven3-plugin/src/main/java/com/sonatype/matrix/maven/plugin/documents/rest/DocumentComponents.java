/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.documents.rest;

import com.sonatype.matrix.rest.plugin.RestComponentProvider;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Configures REST components for the documents subsystem.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
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
