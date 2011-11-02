/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Tom Huybrechts
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
import net.sf.json.JSONObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

public class RunParameterDefinition extends SimpleParameterDefinition {

    private final String projectName;

    @DataBoundConstructor
    public RunParameterDefinition(String name, String projectName, String description) {
        super(name, description);
        this.projectName = projectName;
    }

    @Exported
    public String getProjectName() {
        return projectName;
    }

    public Job getProject() {
        return (Job) Hudson.getInstance().getItem(projectName);
    }

    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.RunParameterDefinition_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/help/parameter/run.html";
        }

        @Override
        public ParameterDefinition newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindJSON(RunParameterDefinition.class, formData);
        }
    }

    @Override
    public ParameterValue getDefaultParameterValue() {
        Run<?,?> lastBuild = getProject().getLastBuild();
        if (lastBuild != null) {
        	return createValue(lastBuild.getExternalizableId());
        } else {
        	return null;
        }
    }

    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        RunParameterValue value = req.bindJSON(RunParameterValue.class, jo);
        value.setDescription(getDescription());
        return value;
    }

    public RunParameterValue createValue(String value) {
        return new RunParameterValue(getName(), value, getDescription());
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && new EqualsBuilder()
            .append(getProjectName(), ((RunParameterDefinition) o).getProjectName())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .appendSuper(super.hashCode())
            .append(getProjectName())
            .toHashCode();
    }
}
