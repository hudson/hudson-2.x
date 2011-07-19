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

import hudson.Extension;
import hudson.FilePath;
import hudson.Functions;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.node_monitors.DiskSpaceMonitorDescriptor.DiskSpace;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.text.ParseException;

/**
 * Checks available disk space of the remote FS root.
 * Requires Mustang.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.123
 */
public class DiskSpaceMonitor extends AbstractDiskSpaceMonitor {
    @DataBoundConstructor
	public DiskSpaceMonitor(String freeSpaceThreshold) throws ParseException {
        super(freeSpaceThreshold);
	}

    public DiskSpaceMonitor() {}
    
    public DiskSpace getFreeSpace(Computer c) {
        return DESCRIPTOR.get(c);
    }

    @Override
    public String getColumnCaption() {
        // Hide this column from non-admins
        return Hudson.getInstance().hasPermission(Hudson.ADMINISTER) ? super.getColumnCaption() : null;
    }

    public static final DiskSpaceMonitorDescriptor DESCRIPTOR = new DiskSpaceMonitorDescriptor() {
        public String getDisplayName() {
            return Messages.DiskSpaceMonitor_DisplayName();
        }

        protected DiskSpace getFreeSpace(Computer c) throws IOException, InterruptedException {
            FilePath p = c.getNode().getRootPath();
            if(p==null) return null;

            return p.act(new GetUsableSpace());
        }
    };

    @Extension
    public static DiskSpaceMonitorDescriptor install() {
        if(Functions.isMustangOrAbove())    return DESCRIPTOR;
        return null;
    }
}
