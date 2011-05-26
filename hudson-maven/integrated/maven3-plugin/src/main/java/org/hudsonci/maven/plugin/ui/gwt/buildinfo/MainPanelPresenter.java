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

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;
import org.hudsonci.maven.model.PropertiesDTO.Entry;
import org.hudsonci.maven.model.state.BuildStateDTO;

import java.util.List;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.MainPanelPresenterImpl;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.MainPanelViewImpl;

/**
 * Presenter for {@link MainPanelView} widgets.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@ImplementedBy(MainPanelPresenterImpl.class)
public interface MainPanelPresenter
{
    void bind(HasWidgets container);

    void setBuildStates(List<BuildStateDTO> buildStates);

    void buildStateSelected(int selectedIndex);

    void selectModuleInfo();

    void refresh();

    /**
     * Main UI panel.
     */
    @ImplementedBy(MainPanelViewImpl.class)
    public interface MainPanelView
        extends IsWidget
    {
        void setFirstShownWidget(IsWidget widget);

        void setVersionData(List<Entry> data);

        void setEnvironmentData(List<Entry> data);

        void setSystemData(List<Entry> data);

        void setUserData(List<Entry> data);

        void setStateSelectionNames(List<String> stateNames);

        void showStatePicker(int selectedIndex);

        void setPresenter(MainPanelPresenter presenter);

        void selectModuleInfo();
    }
}
