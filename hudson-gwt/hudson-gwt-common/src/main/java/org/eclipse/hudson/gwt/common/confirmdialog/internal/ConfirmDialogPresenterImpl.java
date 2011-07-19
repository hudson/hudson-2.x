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

package org.eclipse.hudson.gwt.common.confirmdialog.internal;

import javax.inject.Inject;

import org.eclipse.hudson.gwt.common.confirmdialog.ConfirmDialogPresenter;
import org.eclipse.hudson.gwt.common.confirmdialog.ConfirmDialogView;

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
