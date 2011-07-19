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

package org.eclipse.hudson.plugins.snapshotmonitor.internal;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.hudson.plugins.snapshotmonitor.MetadataChecker;
import org.eclipse.hudson.plugins.snapshotmonitor.SnapshotMonitorPlugin;

/**
 * {@link MetadataChecker} provider.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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
