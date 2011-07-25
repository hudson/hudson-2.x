/*******************************************************************************
 *
 * Copyright (c) 2004-2010, Oracle Corporation.
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

package hudson.cli;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.tasks.HudsonMimeMessage;
import hudson.tasks.Mailer;

/**
 * Sends e-mail through Hudson.
 *
 * <p>
 * Various platforms have different commands to do this, so on heterogenous platform, doing this via Hudson is easier.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class MailCommand extends CLICommand {
    public String getShortDescription() {
        return "Reads stdin and sends that out as an e-mail.";
    }

    protected int run() throws Exception {
        Hudson.getInstance().checkPermission(Item.CONFIGURE);
        Mailer.DescriptorImpl descriptor = Mailer.descriptor();
        descriptor.send(new HudsonMimeMessage(descriptor.createSession(), stdin));
        return 0;
    }
}
