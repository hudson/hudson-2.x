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

package org.eclipse.hudson.gwt.common.confirmdialog;

import com.google.inject.ImplementedBy;

import org.eclipse.hudson.gwt.common.confirmdialog.internal.ConfirmDialogPresenterImpl;

/**
 * Manages displaying a confirmation dialog.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@ImplementedBy(ConfirmDialogPresenterImpl.class)
public interface ConfirmDialogPresenter
{
    ConfirmDialogView getView();

    interface OkCancelCallback
    {
        void onOk();

        void onCancel();
    }

    void confirm(String title, String message, OkCancelCallback callback);
}
