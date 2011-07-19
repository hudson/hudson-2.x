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

/**
 * Shows the version.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class VersionCommand extends CLICommand {
    @Override
    public String getShortDescription() {
        return "Shows the Hudson version";
    }

    protected int run() {
        // CLICommand.main checks Hudson.READ permission.. no other check needed.
        stdout.println(Hudson.VERSION);
        return 0;
    }
}
