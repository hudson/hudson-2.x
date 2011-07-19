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

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;

import org.eclipse.hudson.gwt.common.confirmdialog.ConfirmDialogPresenter.OkCancelCallback;
import org.eclipse.hudson.gwt.common.confirmdialog.internal.ConfirmDialogViewImpl;

/**
 * Provides a confirmation dialog.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@ImplementedBy(ConfirmDialogViewImpl.class)
public interface ConfirmDialogView
    extends IsWidget
{
    @DefaultLocale("en_US")
    interface MessagesResource
        extends Messages
    {
        @DefaultMessage("Ok")
        String ok();

        @DefaultMessage("Cancel")
        String cancel();
    }

    void setTitleMessage(String title);

    void setBodyMessage(String message);

    void show();

    void hide();

    // For now re-using the presenters OkCancelCallback for hooks to the Ok/Cancel buttons

    void setCallback(OkCancelCallback callback);
}
