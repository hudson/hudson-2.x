/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
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
package org.jvnet.hudson.test;

import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

/**
 * Mock {@link Builder} that always cause a build to fail.
 *
 * @author Kohsuke Kawaguchi
 */
public class FailureBuilder extends Builder {
    
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Simulating a failure");
        build.setResult(Result.FAILURE);
        return false;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<Builder> {
        public String getDisplayName() {
            return "Always fail";
        }
        public FailureBuilder newInstance(StaplerRequest req, JSONObject data) {
            return new FailureBuilder();
        }
    }
}
