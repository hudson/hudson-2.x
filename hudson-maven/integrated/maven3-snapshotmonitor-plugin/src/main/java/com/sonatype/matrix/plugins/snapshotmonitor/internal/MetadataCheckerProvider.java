/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.plugins.snapshotmonitor.internal;

import com.sonatype.matrix.plugins.snapshotmonitor.MetadataChecker;
import com.sonatype.matrix.plugins.snapshotmonitor.SnapshotMonitorPlugin;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * {@link MetadataChecker} provider.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.2
 */
public class MetadataCheckerProvider
    implements Provider<MetadataChecker>
{
    private final SnapshotMonitorPlugin plugin;

    @Inject
    public MetadataCheckerProvider(final SnapshotMonitorPlugin plugin) {
        assert plugin != null;
        this.plugin = plugin;
    }

    public MetadataChecker get() {
        return new SimpleMetadataChecker(plugin.getUrl(), plugin.getUserName(), plugin.getPassword());
    }
}