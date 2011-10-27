/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi,
  * id:cactusman, Anton Kozak, Nikita Levyankov
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.model;

import hudson.Extension;
import hudson.util.CascadingUtil;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import org.hudsonci.api.model.IFreeStyleProject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Free-style software project.
 * 
 * @author Kohsuke Kawaguchi
 */
public class FreeStyleProject extends Project<FreeStyleProject,FreeStyleBuild> implements TopLevelItem,
    IFreeStyleProject {

    /**
     * See {@link #setCustomWorkspace(String)}.
     *
     * @since 1.216
     * @deprecated as of 2.2.0
     *             don't use this field directly, logic was moved to {@link org.hudsonci.api.model.IProjectProperty}.
     *             Use getter/setter for accessing to this field.
     */
    @Deprecated
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

    public String getCustomWorkspace() throws IOException {
        return CascadingUtil.getStringProjectProperty(this, CUSTOM_WORKSPACE_PROPERTY_NAME).getValue();
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
     * @param customWorkspace new custom workspace to set
     * @since 1.320
     * @throws IOException if any.
     */
    public void setCustomWorkspace(String customWorkspace) throws IOException {
        CascadingUtil.getStringProjectProperty(this, CUSTOM_WORKSPACE_PROPERTY_NAME).setValue(customWorkspace);
        save();
    }

    @Override
    protected void submit(StaplerRequest req, StaplerResponse rsp)
        throws IOException, ServletException, Descriptor.FormException {
        super.submit(req, rsp);
        setCustomWorkspace(
            req.hasParameter("customWorkspace") ? req.getParameter("customWorkspace.directory") : null);
    }

    @Override
    protected void buildProjectProperties() throws IOException {
        super.buildProjectProperties();
        convertCustomWorkspaceProperty();
    }

    /**
     * Converts customWorkspace property to ProjectProperty.
     *
     * @throws IOException if any.
     */
    void convertCustomWorkspaceProperty() throws IOException {
        if (null != customWorkspace && null == getProperty(CUSTOM_WORKSPACE_PROPERTY_NAME)) {
            setCustomWorkspace(customWorkspace);
            customWorkspace = null;//Reset to null. No longer needed.
        }
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
