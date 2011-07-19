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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.hudson.gwt.common.alertdialog.AlertDialogView;
import org.eclipse.hudson.gwt.common.alertdialog.AlertDialogPresenter.OkCallback;

/**
 * Default implementation of {@link AlertDialogView}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class AlertDialogViewImpl
    implements AlertDialogView
{
    private final DialogBox dialog;

    private final Label bodyText;

    private final Button okButton;

    private OkCallback callback;

    @Inject
    public AlertDialogViewImpl(final MessagesResource messages) {
        assert messages != null;

        dialog = new DialogBox()
        {
            /**
             * Handle ESC key-down to ok.
             */
            @Override
            protected void onPreviewNativeEvent(final Event.NativePreviewEvent event) {
                assert event != null;
                super.onPreviewNativeEvent(event);
                switch (event.getTypeInt()) {
                    case Event.ONKEYDOWN:
                        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                            fireOk();
                        }
                        break;
                }
            }
        };
        dialog.setAnimationEnabled(true);
        dialog.setGlassEnabled(true);
        dialog.setModal(true);

        DomHelper.onTop(dialog.getElement());

        VerticalPanel container = new VerticalPanel();
        dialog.setWidget(container);

        // TODO: Add a nice icon

        // TODO: Make more alert-like

        bodyText = new Label();
        bodyText.setWordWrap(true);
        container.add(bodyText);

        // TODO: Add spacer or update style to provide padding around the message, before the buttons are added

        HorizontalPanel buttonPanel = new HorizontalPanel();
        container.add(buttonPanel);

        okButton = new Button(messages.ok());
        okButton.addClickHandler(new ClickHandler()
        {
            public void onClick(final ClickEvent event) {
                fireOk();
            }
        });
        buttonPanel.add(okButton);
    }

    public Widget asWidget() {
        return dialog;
    }

    public void setTitleMessage(final String title) {
        dialog.setText(title);
    }

    public void setBodyMessage(final String message) {
        bodyText.setText(message);
    }

    public void show() {
        dialog.center();
    }

    public void hide() {
        dialog.hide();
    }

    public void setCallback(final OkCallback callback) {
        this.callback = callback;
    }

    private void fireOk() {
        if (callback != null) {
            callback.onOk();
        }
    }
}
