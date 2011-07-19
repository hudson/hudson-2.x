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

package org.eclipse.hudson.gwt.common.waitdialog.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

import org.eclipse.hudson.gwt.common.alertdialog.internal.DomHelper;
import org.eclipse.hudson.gwt.common.waitdialog.WaitPresenter;

/**
 * Dialog box to display "Please wait...".
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
@Singleton
public class PleaseWaitDialog
    extends DialogBox implements WaitPresenter
{
    @DefaultLocale("en_US")
    public static interface MessagesResource
        extends Messages
    {
        @DefaultMessage("Please wait...")
        String title();
    }

    @Inject
    public PleaseWaitDialog(final MessagesResource messages, final CellTable.Resources resources) {
        assert messages != null;
        assert resources != null;

        setText(messages.title());

        // These are way too distracting
        setAnimationEnabled(false);
        setGlassEnabled(false);

        DomHelper.onTop(getElement());

        FlowPanel panel = new FlowPanel();
        setWidget(panel);

        // Re-use the CellTable's loading image
        Image image = new Image(resources.cellTableLoading());
        panel.add(image);
    }

    public void startWaiting() {
        center();
    }

    public void stopWaiting() {
        hide();
    }
}
