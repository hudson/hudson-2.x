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

import java.util.Map;
import java.util.TreeMap;

/**
 * Show the list of all commands.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class HelpCommand extends CLICommand {
    @Override
    public String getShortDescription() {
        return "Lists all the available commands";
    }

    protected int run() {
        if (!Hudson.getInstance().hasPermission(Hudson.READ)) {
            stderr.println("You must authenticate to access this Hudson.\n"
                    + "Use --username/--password/--password-file parameters or login command.");
            return 0;
        }

        Map<String,CLICommand> commands = new TreeMap<String,CLICommand>();
        for (CLICommand c : CLICommand.all())
            commands.put(c.getName(),c);

        for (CLICommand c : commands.values()) {
            stderr.println("  "+c.getName());
            stderr.println("    "+c.getShortDescription());
        }
        
        return 0;
    }
}
