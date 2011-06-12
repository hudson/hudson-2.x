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

package org.hudsonci.maven.plugin.ui.gwt.configure.workspace.internal;

import com.google.gwt.user.client.ui.HasWidgets;

import org.hudsonci.maven.plugin.ui.gwt.configure.workspace.WorkspaceManagerPresenter;
import org.hudsonci.maven.plugin.ui.gwt.configure.workspace.WorkspaceManagerView;
import org.hudsonci.maven.plugin.ui.gwt.configure.workspace.WorkspacePresenter;
import org.sonatype.inject.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link WorkspaceManagerPresenter}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Singleton
public class WorkspaceManagerPresenterImpl
    implements WorkspaceManagerPresenter
{
    private final WorkspaceManagerView view;

    private final WorkspacePresenter defaultWorkspace;

    @Inject
    public WorkspaceManagerPresenterImpl(final WorkspaceManagerView view, final @Named("default") @Nullable WorkspacePresenter defaultWorkspace) {
        this.view = checkNotNull(view);
        this.defaultWorkspace = defaultWorkspace;
        view.setPresenter(this);
    }

    public WorkspaceManagerView getView() {
        return view;
    }

    public void start(final HasWidgets container) {
        checkNotNull(container);
        container.clear();
        container.add(view.asWidget());

        if (defaultWorkspace != null) {
            view.add(defaultWorkspace.getView());
        }
    }
}
