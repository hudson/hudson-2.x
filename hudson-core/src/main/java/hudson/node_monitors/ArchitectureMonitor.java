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
*    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.node_monitors;

import hudson.model.Computer;
import hudson.model.Descriptor.FormException;
import hudson.remoting.Callable;
import hudson.Extension;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

/**
 * Discovers the architecture of the system to display in the slave list page.
 *
 * @author Kohsuke Kawaguchi
 */
public class ArchitectureMonitor extends NodeMonitor {
    @Extension
    public static final class DescriptorImpl extends AbstractNodeMonitorDescriptor<String> {
        protected String monitor(Computer c) throws IOException, InterruptedException {
            return c.getChannel().call(new GetArchTask());
        }

        public String getDisplayName() {
            return Messages.ArchitectureMonitor_DisplayName();
        }

        public NodeMonitor newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new ArchitectureMonitor();
        }
    }

    /**
     * Obtains the string that represents the architecture.
     */
    private static class GetArchTask implements Callable<String,RuntimeException> {
        public String call() {
            String os = System.getProperty("os.name");
            String arch = System.getProperty("os.arch");
            return os+" ("+arch+')';
        }

        private static final long serialVersionUID = 1L;
    }
}
