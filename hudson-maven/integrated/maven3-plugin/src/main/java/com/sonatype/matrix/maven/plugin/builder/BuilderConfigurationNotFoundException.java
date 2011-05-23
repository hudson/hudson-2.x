/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.builder;

import com.sonatype.matrix.service.NotFoundException;

/**
 * Thrown when a requested builder configuration is not found.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
public class BuilderConfigurationNotFoundException
    extends NotFoundException
{
    public BuilderConfigurationNotFoundException(final String projectName, final int index) {
        super(String.format("No such builder configuration for project '%s' at index %s", projectName, index));
    }
}