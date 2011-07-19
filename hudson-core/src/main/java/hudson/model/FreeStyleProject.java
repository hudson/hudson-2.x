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
*    Kohsuke Kawaguchi, id:cactusman
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.Extension;

import java.io.File;
import java.io.IOException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;

/**
 * Free-style software project.
 * 
 * @author Kohsuke Kawaguchi
 */
public class FreeStyleProject extends Project<FreeStyleProject,FreeStyleBuild> implements TopLevelItem {
    /**
     * See {@link #setCustomWorkspace(String)}.
     *
     * @since 1.216
     */
    private String customWorkspace;

    /**
     * @deprecated as of 1.390
     */
    public FreeStyleProject(Hudson parent, String name) {
        super(parent, name);
    }

    public FreeStyleProject(ItemGroup parent, String name) {
        super(parent, name);
    }

    @Override
    protected Class<FreeStyleBuild> getBuildClass() {
        return FreeStyleBuild.class;
    }

    public String getCustomWorkspace() {
        return customWorkspace;
    }

    /**
     * User-specified workspace directory, or null if it's up to Hudson.
     *
     * <p>
     * Normally a free-style project uses the workspace location assigned by its parent container,
     * but sometimes people have builds that have hard-coded paths (which can be only built in
     * certain locations. see http://www.nabble.com/Customize-Workspace-directory-tt17194310.html for
     * one such discussion.)
     *
     * <p>
     * This is not {@link File} because it may have to hold a path representation on another OS.
     *
     * <p>
     * If this path is relative, it's resolved against {@link Node#getRootPath()} on the node where this workspace
     * is prepared. 
     *
     * @since 1.320
     */
    public void setCustomWorkspace(String customWorkspace) throws IOException {
        this.customWorkspace= customWorkspace;
        save();
    }

    @Override
    protected void submit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
        if(req.hasParameter("customWorkspace"))
            customWorkspace = req.getParameter("customWorkspace.directory");
        else
            customWorkspace = null;

        super.submit(req, rsp);
    }

    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension(ordinal=1000)
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends AbstractProjectDescriptor {
        public String getDisplayName() {
            return Messages.FreeStyleProject_DisplayName();
        }

        public FreeStyleProject newInstance(ItemGroup parent, String name) {
            return new FreeStyleProject(parent,name);
        }
    }
}
