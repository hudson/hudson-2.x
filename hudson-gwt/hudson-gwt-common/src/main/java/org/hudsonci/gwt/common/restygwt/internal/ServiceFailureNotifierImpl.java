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

package org.hudsonci.gwt.common.restygwt.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.http.client.Response;
import org.hudsonci.gwt.common.alertdialog.AlertDialogPresenter;
import org.hudsonci.gwt.common.restygwt.ServiceFailureNotifier;
import org.hudsonci.gwt.common.waitdialog.WaitPresenter;

import org.fusesource.restygwt.client.Method;

/**
 * Default implementation of {@link ServiceFailureNotifier}.
 * 
 * The failure will be logged and the {@link WaitPresenter#stopWaiting()} will be called when necessary.
 * 
 * // FIXME: Probably want a generic component to handle operation success/failure bits and show/hide of the wait dialog.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@Singleton
public class ServiceFailureNotifierImpl implements ServiceFailureNotifier{

    private final AlertDialogPresenter alertDialogPresenter;
    private final MessagesResource messages;
    private final WaitPresenter waitWidget;

    @Inject
    public ServiceFailureNotifierImpl(final AlertDialogPresenter alertDialogPresenter, final MessagesResource messages, final WaitPresenter waitWidget) {
        assert alertDialogPresenter != null;
        this.alertDialogPresenter = alertDialogPresenter;
        assert messages != null;
        this.messages = messages;
        assert waitWidget != null;
        this.waitWidget = waitWidget;
    }
    
    public void displayFailure(final String message, final Method method, final Throwable cause) {
        assert message != null;
        assert cause != null;
        assert method != null;

        String status = getStatusMessage(method);
        Log.error(message + "; with status: " + status, cause);

        waitWidget.stopWaiting();
        alertDialogPresenter.alert(messages.failureTitle(), messages.failureReason(message, status));
    }

    private String getStatusMessage(final Method method) {
        assert method != null;
        Response resp = method.getResponse();

        StringBuilder status = new StringBuilder();
        
        // When the destination is unreachable (e.g. server not on) the status code is 0.
        // Prefer a better API for checking this but there's nothing at the moment.
        if(resp.getStatusCode() == 0){
            status.append(messages.noStatus());
        }
        else {
            if (resp.getStatusText() != null) {
                status.append(resp.getStatusText());
            }
            status.append("[").append(resp.getStatusCode()).append("]");
        }

        return status.toString();
    }        
}
