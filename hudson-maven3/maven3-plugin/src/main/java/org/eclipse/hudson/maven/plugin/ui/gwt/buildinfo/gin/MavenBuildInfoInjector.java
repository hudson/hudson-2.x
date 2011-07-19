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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.gin;

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.MavenBuildInfoController;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

/**
 * Ginjector for the Maven build information application. 
 *
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@GinModules(MavenBuildInfoModule.class)
public interface MavenBuildInfoInjector
    extends Ginjector
{
    Scheduler getScheduler();

    MavenBuildInfoController getAppController();
}
