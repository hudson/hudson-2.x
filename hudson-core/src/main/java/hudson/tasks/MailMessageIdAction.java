/*******************************************************************************
 *
 * Copyright (c) 2004-2009 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
*
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.tasks;

import hudson.model.Action;

/**
 * Remembers the message ID of the e-mail that was sent for the build.
 *
 * <p>
 * This allows us to send further updates as replies.
 *
 * @author Kohsuke Kawaguchi
 */
public class MailMessageIdAction implements Action {
    /**
     * Message ID of the e-mail sent for the build.
     */
    public final String messageId;

    public MailMessageIdAction(String messageId) {
        this.messageId = messageId;
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return "Message Id"; // but this is never supposed to be displayed
    }

    public String getUrlName() {
        return null; // no web binding
    }
}
