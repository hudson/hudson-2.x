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

package org.eclipse.hudson.maven.plugin.ui.gwt.buildinfo.internal;

import com.google.gwt.view.client.ListDataProvider;
import org.eclipse.hudson.maven.model.state.ArtifactDTO;

import javax.inject.Singleton;

/**
 * Strongly typed data provider for {@link ArtifactDTO}s.
 *
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Singleton
public class ArtifactDataProvider extends ListDataProvider<ArtifactDTO>
{
}
