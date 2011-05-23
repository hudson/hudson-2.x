/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */
package com.sonatype.matrix.maven.plugin.ui.gwt.configure.builder;

import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.plugin.ui.gwt.configure.builder.internal.MavenBuilderConfigurationPresenterImpl;

/**
 * ???
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 1.1
 */
@ImplementedBy(MavenBuilderConfigurationPresenterImpl.class)
public interface MavenBuilderConfigurationPresenter
{
    MavenBuilderConfigurationView getView();
}
