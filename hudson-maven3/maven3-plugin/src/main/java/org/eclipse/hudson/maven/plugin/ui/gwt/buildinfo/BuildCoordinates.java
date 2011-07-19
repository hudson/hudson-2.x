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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo;

import org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal.GwtLoadedBuildCoordinates;

import com.google.inject.ImplementedBy;

/**
 * Information to locate a build in Hudson.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@ImplementedBy(GwtLoadedBuildCoordinates.class)
public interface BuildCoordinates
{
    String getProjectName();

    int getBuildnumber();
}
