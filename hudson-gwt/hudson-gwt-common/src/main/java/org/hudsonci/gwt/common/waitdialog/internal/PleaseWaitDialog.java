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

package org.hudsonci.gwt.common.waitdialog.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import org.hudsonci.gwt.common.alertdialog.internal.DomHelper;
import org.hudsonci.gwt.common.waitdialog.WaitPresenter;

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
