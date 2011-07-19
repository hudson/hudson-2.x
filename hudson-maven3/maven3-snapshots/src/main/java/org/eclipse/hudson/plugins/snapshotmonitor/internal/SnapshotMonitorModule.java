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

import com.google.inject.AbstractModule;

import javax.inject.Named;

import org.eclipse.hudson.plugins.snapshotmonitor.MetadataChecker;

/**
 * Snapshot monitor module.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
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
