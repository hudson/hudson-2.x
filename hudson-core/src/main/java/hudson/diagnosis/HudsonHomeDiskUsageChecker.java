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

package hudson.diagnosis;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.PeriodicWork;
import org.jvnet.animal_sniffer.IgnoreJRERequirement;

import java.util.logging.Logger;

/**
 * Periodically checks the disk usage of <tt>HUDSON_HOME</tt>,
 * and activate {@link HudsonHomeDiskUsageMonitor} if necessary.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class HudsonHomeDiskUsageChecker extends PeriodicWork {
    public long getRecurrencePeriod() {
        return HOUR;
    }

    @IgnoreJRERequirement
    protected void doRun() {
        try {
            long free = Hudson.getInstance().getRootDir().getUsableSpace();
            long total = Hudson.getInstance().getRootDir().getTotalSpace();
            if(free<=0 || total<=0) {
                // information unavailable. pointless to try.
                LOGGER.info("HUDSON_HOME disk usage information isn't available. aborting to monitor");
                cancel();
                return;
            }

            LOGGER.fine("Monitoring disk usage of HUDSON_HOME. total="+total+" free="+free);


            // if it's more than 90% full and less than the minimum, activate
            // it's AND and not OR so that small Hudson home won't get a warning,
            // and similarly, if you have a 1TB disk, you don't get a warning when you still have 100GB to go.
            HudsonHomeDiskUsageMonitor.get().activated = (total/free>10 && free< FREE_SPACE_THRESHOLD);
        } catch (LinkageError _) {
            // pre Mustang
            LOGGER.info("Not on JDK6. Cannot monitor HUDSON_HOME disk usage");
            cancel();
        }
    }

    private static final Logger LOGGER = Logger.getLogger(HudsonHomeDiskUsageChecker.class.getName());

    /**
     * Gets the minimum amount of space to check for, with a default of 1GB
     */
    public static long FREE_SPACE_THRESHOLD = Long.getLong(
            HudsonHomeDiskUsageChecker.class.getName() + ".freeSpaceThreshold",
            1024L*1024*1024);

}
