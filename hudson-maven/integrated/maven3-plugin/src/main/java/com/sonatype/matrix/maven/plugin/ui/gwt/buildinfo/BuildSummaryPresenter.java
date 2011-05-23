/**
 * Sonatype Hudson Professional (TM)
 * Copyright (C) 2010-2011 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/hudson/attributions/.
 * "Sonatype" and "Sonatype Hudson Professional" are trademarks of Sonatype, Inc.
 * "Hudson" is a trademark of Oracle, Inc.
 */

package com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;
import com.sonatype.matrix.maven.model.state.BuildStateDTO;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.BuildSummaryPresenterImpl;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.BuildSummaryViewImpl;
import com.sonatype.matrix.maven.plugin.ui.gwt.buildinfo.internal.ModuleDataProvider;

/**
 * Display summary information about a build.
 * 
 * @author Jamie Whitehouse
 * @since 1.1
 */
@ImplementedBy(BuildSummaryPresenterImpl.class)
public interface BuildSummaryPresenter
{
    @ImplementedBy(BuildSummaryViewImpl.class)
    public interface BuildSummaryView
        extends IsWidget
    {
        void setModuleData(ModuleDataProvider dataProvider);

        void setBuildSummaryText(String summaryText);
    }

    void setBuildState(BuildStateDTO buildState);
}