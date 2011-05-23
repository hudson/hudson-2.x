/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo;

import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.GwtLoadedBuildCoordinates;

/**
 * Information to locate a build in Matrix.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
@ImplementedBy(GwtLoadedBuildCoordinates.class)
public interface BuildCoordinates
{
    String getProjectName();

    int getBuildnumber();
}