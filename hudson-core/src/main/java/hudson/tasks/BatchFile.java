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
 *    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.tasks;

import hudson.FilePath;
import hudson.Extension;
import hudson.model.AbstractProject;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Executes commands by using Windows batch file.
 *
 * @author Kohsuke Kawaguchi
 */
public class BatchFile extends CommandInterpreter {
    @DataBoundConstructor
    public BatchFile(String command) {
        super(command);
    }

    public String[] buildCommandLine(FilePath script) {
        return new String[] {"cmd","/c","call",script.getRemote()};
    }

    protected String getContents() {
        return command+"\r\nexit %ERRORLEVEL%";
    }

    protected String getFileExtension() {
        return ".bat";
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public String getHelpFile() {
            return "/help/project-config/batch.html";
        }

        public String getDisplayName() {
            return Messages.BatchFile_DisplayName();
        }

        @Override
        public Builder newInstance(StaplerRequest req, JSONObject data) {
            return new BatchFile(data.getString("command"));
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
