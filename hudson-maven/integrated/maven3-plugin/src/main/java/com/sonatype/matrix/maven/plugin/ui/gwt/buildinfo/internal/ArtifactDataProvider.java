/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal;

import com.google.gwt.view.client.ListDataProvider;
import com.sonatype.matrix.maven.model.state.ArtifactDTO;

import javax.inject.Singleton;

/**
 * Strongly typed data provider for {@link ArtifactDTO}s.
 *
 * @author Jamie Whitehouse
 * @since 1.1
 */
@Singleton
public class ArtifactDataProvider extends ListDataProvider<ArtifactDTO>
{
}
