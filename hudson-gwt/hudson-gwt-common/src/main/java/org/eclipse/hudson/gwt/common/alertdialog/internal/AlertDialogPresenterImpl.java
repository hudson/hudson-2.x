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

package org.eclipse.hudson.gwt.common.alertdialog.internal;

import javax.inject.Inject;

import org.eclipse.hudson.gwt.common.alertdialog.AlertDialogPresenter;
import org.eclipse.hudson.gwt.common.alertdialog.AlertDialogView;

/**
 * Default implementation of {@link AlertDialogPresenter}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class AlertDialogPresenterImpl
    implements AlertDialogPresenter
{
    private final AlertDialogView view;

    private OkCallback callback;

    @Inject
    public AlertDialogPresenterImpl(final AlertDialogView view) {
        assert view != null;
        this.view = view;

        // Hook up the view's callback to our user callback + hide the view
        view.setCallback(new OkCallback()
        {
            public void onOk() {
                if (callback != null) {
                    callback.onOk();
                }
                view.hide();
            }
        });
    }

    public AlertDialogView getView() {
        return view;
    }

    public void alert(final String title, final String message, final OkCallback callback) {
        view.setTitleMessage(title);
        view.setBodyMessage(message);
        this.callback = callback;
        view.show();
    }

    public void alert(final String title, final String message) {
        alert(title, message, null);
    }
}
