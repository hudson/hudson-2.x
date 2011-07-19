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
*    Kohsuke Kawaguchi, Seiji Sogabe, Tom Huybrechts
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import hudson.Extension;
import hudson.FilePath;
import hudson.cli.CLICommand;

import java.io.IOException;
import java.io.File;

/**
 * {@link ParameterDefinition} for doing file upload.
 *
 * <p>
 * The file will be then placed into the workspace at the beginning of a build.
 *
 * @author Kohsuke Kawaguchi
 */
public class FileParameterDefinition extends ParameterDefinition {
    @DataBoundConstructor
    public FileParameterDefinition(String name, String description) {
        super(name, description);
    }

    public FileParameterValue createValue(StaplerRequest req, JSONObject jo) {
        FileParameterValue p = req.bindJSON(FileParameterValue.class, jo);
        p.setLocation(getName());
        p.setDescription(getDescription());
        return p;
    }

    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.FileParameterDefinition_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/help/parameter/file.html";
        }
    }

	@Override
	public ParameterValue createValue(StaplerRequest req) {
		throw new UnsupportedOperationException();
	}

    @Override
    public ParameterValue createValue(CLICommand command, String value) throws IOException, InterruptedException {
        // capture the file to the server
        FilePath src = new FilePath(command.channel,value);
        File local = File.createTempFile("hudson","parameter");
        src.copyTo(new FilePath(local));

        FileParameterValue p = new FileParameterValue(getName(), local, src.getName());
        p.setDescription(getDescription());
        p.setLocation(getName());
        return p;
    }
}
