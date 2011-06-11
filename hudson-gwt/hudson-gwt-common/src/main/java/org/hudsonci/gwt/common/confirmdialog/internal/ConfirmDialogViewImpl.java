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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.hudsonci.gwt.common.confirmdialog.ConfirmDialogPresenter.OkCancelCallback;
import org.hudsonci.gwt.common.confirmdialog.ConfirmDialogView;

/**
 * Default implementation of {@link ConfirmDialogView}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.1.0
 */
public class ConfirmDialogViewImpl
    implements ConfirmDialogView
{
    private final DialogBox dialog;

    private final Label bodyText;

    private final Button okButton;

    private final Button cancelButton;

    private OkCancelCallback callback;

    @Inject
    public ConfirmDialogViewImpl(final MessagesResource messages) {
        assert messages != null;

        dialog = new DialogBox()
        {
            /**
             * Handle ESC key-down to cancel.
             */
            @Override
            protected void onPreviewNativeEvent(final Event.NativePreviewEvent event) {
                assert event != null;
                super.onPreviewNativeEvent(event);
                switch (event.getTypeInt()) {
                    case Event.ONKEYDOWN:
                        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                            fireCancel();
                        }
                        break;
                }
            }
        };
        dialog.setAnimationEnabled(true);
        dialog.setGlassEnabled(true);
        dialog.setModal(true);

        // Make sure the dialog is always on top
        DOM.setIntStyleAttribute(dialog.getElement(), "z-index", Integer.MAX_VALUE);

        VerticalPanel container = new VerticalPanel();
        dialog.setWidget(container);

        // TODO: Add a nice icon

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

        cancelButton = new Button(messages.cancel());
        cancelButton.addClickHandler(new ClickHandler()
        {
            public void onClick(final ClickEvent event) {
                fireCancel();
            }
        });
        buttonPanel.add(cancelButton);
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

    public void setCallback(final OkCancelCallback callback) {
        this.callback = callback;
    }

    private void fireOk() {
        if (callback != null) {
            callback.onOk();
        }
    }

    private void fireCancel() {
        if (callback != null) {
            callback.onCancel();
        }
    }
}
