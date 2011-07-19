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

package org.eclipse.hudson.gwt.common.restygwt.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.http.client.Response;

import org.eclipse.hudson.gwt.common.alertdialog.AlertDialogPresenter;
import org.eclipse.hudson.gwt.common.restygwt.ServiceFailureNotifier;
import org.eclipse.hudson.gwt.common.waitdialog.WaitPresenter;
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
