/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.cli;

import hudson.Extension;

/**
 * Deletes the credential stored with the login command.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.351
 */
@Extension
public class LogoutCommand extends CLICommand {
    @Override
    public String getShortDescription() {
        return "Deletes the credential stored with the login command";
    }

    @Override
    protected int run() throws Exception {
        ClientAuthenticationCache store = new ClientAuthenticationCache(channel);
        store.remove();
        return 0;
    }
}
