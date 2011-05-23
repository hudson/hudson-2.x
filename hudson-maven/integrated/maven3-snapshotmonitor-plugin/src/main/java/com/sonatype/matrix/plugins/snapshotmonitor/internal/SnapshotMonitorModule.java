/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.plugins.snapshotmonitor.internal;

import com.google.inject.AbstractModule;
import com.sonatype.matrix.plugins.snapshotmonitor.MetadataChecker;

import javax.inject.Named;

/**
 * Snapshot monitor module.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.2
 */
@Named
public class SnapshotMonitorModule
    extends AbstractModule
{
    @Override
    protected void configure() {
        bind(MetadataChecker.class).toProvider(MetadataCheckerProvider.class);
    }
}