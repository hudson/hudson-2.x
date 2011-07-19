/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.slaves;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * Executes a program on the master and expect that script to connect.
 *
 * @author Kohsuke Kawaguchi
 */
public class CommandConnector extends ComputerConnector {
    public final String command;

    @DataBoundConstructor
    public CommandConnector(String command) {
        this.command = command;
    }

    @Override
    public CommandLauncher launch(String host, TaskListener listener) throws IOException, InterruptedException {
        return new CommandLauncher(command,new EnvVars("SLAVE",host));
    }

    @Extension
    public static class DescriptorImpl extends ComputerConnectorDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.CommandLauncher_displayName();
        }
    }
}
