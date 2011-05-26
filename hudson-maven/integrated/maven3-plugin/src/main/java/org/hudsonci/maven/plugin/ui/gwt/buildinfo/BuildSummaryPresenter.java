/**
 * The MIT License
 *
 * Copyright (c) 2010-2011 Sonatype, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.hudsonci.maven.plugin.ui.gwt.buildinfo;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.BuildSummaryPresenterImpl;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.BuildSummaryViewImpl;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.ModuleDataProvider;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;
import org.hudsonci.maven.model.state.BuildStateDTO;

/**
 * Display summary information about a build.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
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
