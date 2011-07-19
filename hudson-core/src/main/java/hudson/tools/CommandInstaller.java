/*******************************************************************************
 *
 * Copyright (c) 2009, Oracle Corporation
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

package hudson.tools;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tasks.CommandInterpreter;
import hudson.util.FormValidation;
import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Installs a tool by running an arbitrary shell command.
 * @since 1.305
 */
public class CommandInstaller extends ToolInstaller {

    /**
     * Command to execute, similar to {@link CommandInterpreter#command}.
     */
    private final String command;

    /**
     * Resulting tool home directory.
     */
    private final String toolHome;

    @DataBoundConstructor
    public CommandInstaller(String label, String command, String toolHome) {
        super(label);
        this.command = command;
        this.toolHome = toolHome;
    }

    public String getCommand() {
        return command;
    }

    public String getToolHome() {
        return toolHome;
    }

    public FilePath performInstallation(ToolInstallation tool, Node node, TaskListener log) throws IOException, InterruptedException {
        FilePath dir = preferredLocation(tool, node);
        // XXX support Windows batch scripts, Unix scripts with interpreter line, etc. (see CommandInterpreter subclasses)
        FilePath script = dir.createTextTempFile("hudson", ".sh", command);
        try {
            String[] cmd = {"sh", "-e", script.getRemote()};
            int r = node.createLauncher(log).launch().cmds(cmd).stdout(log).pwd(dir).join();
            if (r != 0) {
                throw new IOException("Command returned status " + r);
            }
        } finally {
            script.delete();
        }
        return dir.child(toolHome);
    }

    @Extension
    public static class DescriptorImpl extends ToolInstallerDescriptor<CommandInstaller> {

        public String getDisplayName() {
            return Messages.CommandInstaller_DescriptorImpl_displayName();
        }

        public FormValidation doCheckCommand(@QueryParameter String value) {
            if (value.length() > 0) {
                return FormValidation.ok();
            } else {
                return FormValidation.error(Messages.CommandInstaller_no_command());
            }
        }

        public FormValidation doCheckToolHome(@QueryParameter String value) {
            if (value.length() > 0) {
                return FormValidation.ok();
            } else {
                return FormValidation.error(Messages.CommandInstaller_no_toolHome());
            }
        }

    }

}
