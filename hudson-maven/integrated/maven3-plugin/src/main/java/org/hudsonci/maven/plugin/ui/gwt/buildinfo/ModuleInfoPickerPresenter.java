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

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.ImplementedBy;
import org.hudsonci.maven.model.state.MavenProjectDTO;

import org.sonatype.inject.Nullable;

import org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.ModuleDataProvider;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.ModuleInfoPickerPresenterImpl;
import org.hudsonci.maven.plugin.ui.gwt.buildinfo.internal.ModuleInfoPickerTableView;

/**
 * Presenter for {@link ModuleInfoPickerView} widgets.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@ImplementedBy(ModuleInfoPickerPresenterImpl.class)
public interface ModuleInfoPickerPresenter
{
    /**
     * Configures the view.
     * 
     * AKA Binds the presenter to the view.
     * 
     * @return the configured widget
     */
    ModuleInfoPickerView bind();

    /**
     * Indicates that the given module was selected.
     * 
     * Used to wire up the view selection events to the presenters logic.
     * 
     * @param pickedModule the module that was selected or null if one was deselected.
     */
    void moduleSelected(@Nullable MavenProjectDTO pickedModule);

    /**
     * Select the specified module. Used to communicate selection change from other components.
     * Callers should not invoke selectModule and moduleSelected, the latter will be called as
     * needed when this method is invoked.
     */
    void selectModule(MavenProjectDTO module);

    void refreshSelection();

    /**
     * Display of module selector to show a summary of the modules for selection to view details.
     */
    @ImplementedBy(ModuleInfoPickerTableView.class)
    public interface ModuleInfoPickerView
        extends IsWidget
    {
        void setSelectionModel(SelectionModel<? super MavenProjectDTO> selectionModel);

        void setData(ModuleDataProvider mdp);
    }
}
