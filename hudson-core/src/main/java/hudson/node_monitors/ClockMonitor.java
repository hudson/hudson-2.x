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
*    Kohsuke Kawaguchi, Thomas J. Black
 *     
 *
 *******************************************************************************/ 

package hudson.node_monitors;

import hudson.model.Computer;
import hudson.model.Node;
import hudson.util.ClockDifference;
import hudson.Extension;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

import net.sf.json.JSONObject;

/**
 * {@link NodeMonitor} that checks clock of {@link Node} to
 * detect out of sync clocks.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.123
 */
public class ClockMonitor extends NodeMonitor {
    public ClockDifference getDifferenceFor(Computer c) {
        return DESCRIPTOR.get(c);
    }

    @Extension
    public static final AbstractNodeMonitorDescriptor<ClockDifference> DESCRIPTOR = new AbstractNodeMonitorDescriptor<ClockDifference>() {
        protected ClockDifference monitor(Computer c) throws IOException, InterruptedException {
            Node n = c.getNode();
            if(n==null) return null;
            return n.getClockDifference();
        }

        public String getDisplayName() {
            return Messages.ClockMonitor_DisplayName();
        }

        @Override
        public NodeMonitor newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new ClockMonitor();
        }
    };
}
