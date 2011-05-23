/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder.rest;

import com.sonatype.matrix.rest.plugin.RestComponentProvider;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Configures REST components for the builder subsystem.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@Named
@Singleton
public class BuilderComponents
    extends RestComponentProvider
{
    @Override
    public Class<?>[] getClasses() {
        return new Class[] {
            BuilderDefaultConfigResource.class,
            BuilderConfigResource.class,
            BuildStateResource.class
        };
    }
}
