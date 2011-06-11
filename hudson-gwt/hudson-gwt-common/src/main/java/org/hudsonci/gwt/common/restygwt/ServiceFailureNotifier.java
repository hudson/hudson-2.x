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

package org.hudsonci.gwt.common.restygwt;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.Messages;
import com.google.inject.ImplementedBy;
import org.hudsonci.gwt.common.restygwt.internal.ServiceFailureNotifierImpl;

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
