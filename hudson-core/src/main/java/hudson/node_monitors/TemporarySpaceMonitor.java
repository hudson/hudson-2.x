/*******************************************************************************
 *
 * Copyright (c) 2004-2009, Oracle Corporation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *
 *   
 *        
 *
 *******************************************************************************/ 

package hudson.node_monitors;

import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Functions;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.node_monitors.DiskSpaceMonitorDescriptor.DiskSpace;
import hudson.remoting.VirtualChannel;
import org.jvnet.animal_sniffer.IgnoreJRERequirement;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * Monitors the disk space of "/tmp".
 *
 * @author Kohsuke Kawaguchi
 */
public class TemporarySpaceMonitor extends AbstractDiskSpaceMonitor {
    @DataBoundConstructor
	public TemporarySpaceMonitor(String freeSpaceThreshold) throws ParseException {
        super(freeSpaceThreshold);
	}

    public TemporarySpaceMonitor() {}

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
            return Messages.TemporarySpaceMonitor_DisplayName();
        }

        protected DiskSpace getFreeSpace(Computer c) throws IOException, InterruptedException {
            FilePath p = c.getNode().getRootPath();
            if(p==null) return null;

            return p.act(new GetTempSpace());
        }
    };

    @Extension
    public static DiskSpaceMonitorDescriptor install() {
        if(Functions.isMustangOrAbove())    return DESCRIPTOR;
        return null;
    }
    
    protected static final class GetTempSpace implements FileCallable<DiskSpace> {
        @IgnoreJRERequirement
        public DiskSpace invoke(File f, VirtualChannel channel) throws IOException {
            try {
                // if the disk is really filled up we can't even create a single file,
                // so calling File.createTempFile and figuring out the directory won't reliably work.
                f = new File(System.getProperty("java.io.tmpdir"));
                long s = f.getUsableSpace();
                if(s<=0)    return null;
                return new DiskSpace(s);
            } catch (LinkageError e) {
                // pre-mustang
                return null;
            }
        }
        private static final long serialVersionUID = 1L;
    }
}
