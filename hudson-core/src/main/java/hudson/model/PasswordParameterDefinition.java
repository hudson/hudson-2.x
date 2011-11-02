/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Romain Seguy
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
import hudson.util.Secret;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Parameter whose value is a {@link Secret} and is hidden from the UI.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.319
 */
public class PasswordParameterDefinition extends SimpleParameterDefinition {

    private Secret defaultValue;

    @DataBoundConstructor
    public PasswordParameterDefinition(String name, String defaultValue, String description) {
        super(name, description);
        this.defaultValue = Secret.fromString(defaultValue);
    }

    @Override
    public ParameterValue createValue(String value) {
        return new PasswordParameterValue(getName(), value, getDescription());
    }

    @Override
    public PasswordParameterValue createValue(StaplerRequest req, JSONObject jo) {
        PasswordParameterValue value = req.bindJSON(PasswordParameterValue.class, jo);
        value.setDescription(getDescription());
        return value;
    }

    @Override
    public ParameterValue getDefaultParameterValue() {
        return new PasswordParameterValue(getName(), getDefaultValue(), getDescription());
    }

    public String getDefaultValue() {
        return Secret.toString(defaultValue);
    }

    // kept for backward compatibility
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = Secret.fromString(defaultValue);
    }

    @Extension
    public final static class ParameterDescriptorImpl extends ParameterDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.PasswordParameterDefinition_DisplayName();
        }
        
        @Override
        public String getHelpFile() {
            return "/help/parameter/string.html";
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && new EqualsBuilder()
            .append(getDefaultValue(), ((PasswordParameterDefinition) o).getDefaultValue())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .appendSuper(super.hashCode())
            .append(getDefaultValue())
            .toHashCode();
    }
}
