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

package org.eclipse.hudson.gwt.common.restygwt;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.Messages;
import com.google.inject.ImplementedBy;

import org.eclipse.hudson.gwt.common.restygwt.internal.ServiceFailureNotifierImpl;
import org.fusesource.restygwt.client.Method;

/**
 * Manages user notification of RestyGWT service request failures.
 * 
 * @author Jamie Whitehouse
 * @since 2.1.0
 */
@ImplementedBy(ServiceFailureNotifierImpl.class)
public interface ServiceFailureNotifier
{
    @DefaultLocale("en_US")
    interface MessagesResource
        extends Messages
    {
        @DefaultMessage("Operation Failed")
        String failureTitle();

        @DefaultMessage("{0} ({1})")
        String failureReason(String message, String status);
        
        @DefaultMessage("Status unavailable")
        String noStatus();
    }

    void displayFailure(String message, Method method, Throwable cause);
}
