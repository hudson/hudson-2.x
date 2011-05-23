/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.gin;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.MavenBuildInfoController;

/**
 * Ginjector for the Maven build information application. 
 *
 * @author Jamie Whitehouse
 * @since 1.1
 */
@GinModules(MavenBuildInfoModule.class)
public interface MavenBuildInfoInjector
    extends Ginjector
{
    Scheduler getScheduler();

    MavenBuildInfoController getAppController();
}
