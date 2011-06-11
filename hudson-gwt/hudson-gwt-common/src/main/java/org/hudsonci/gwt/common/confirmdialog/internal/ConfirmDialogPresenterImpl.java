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

package org.hudsonci.gwt.common.confirmdialog.internal;

import javax.inject.Inject;

import org.hudsonci.gwt.common.confirmdialog.ConfirmDialogPresenter;
import org.hudsonci.gwt.common.confirmdialog.ConfirmDialogView;

/**
 * Default implementation of {@link ConfirmDialogPresenter}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ConfirmDialogPresenterImpl
    implements ConfirmDialogPresenter
{
    private final ConfirmDialogView view;

    private OkCancelCallback callback;

    @Inject
    public ConfirmDialogPresenterImpl(final ConfirmDialogView view) {
        assert view != null;
        this.view = view;

        // Hook up the view's callback to our user callback + hide the view
        view.setCallback(new OkCancelCallback()
        {
            public void onOk() {
                assert callback != null;
                callback.onOk();
                view.hide();
            }

            public void onCancel() {
                assert callback != null;
                callback.onCancel();
                view.hide();
            }
        });
    }

    public ConfirmDialogView getView() {
        return view;
    }

    public void confirm(final String title, final String message, final OkCancelCallback callback) {
        view.setTitleMessage(title);
        view.setBodyMessage(message);
        this.callback = callback;
        view.show();
    }
}
