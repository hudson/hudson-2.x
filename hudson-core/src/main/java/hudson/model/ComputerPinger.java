/*******************************************************************************
 *
 * Copyright (c) 2004-2010 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     
 *
 *******************************************************************************/ 

package hudson.model;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * A way to see if a computer is reachable.
 *
 * @since 1.378
 */
public abstract class ComputerPinger implements ExtensionPoint {
    /**
     * Is the specified address reachable?
     *
     * @param ia      The address to check.
     * @param timeout Timeout in seconds.
     */
    public abstract boolean isReachable(InetAddress ia, int timeout) throws IOException;

    /**
     * Get all registered instances.
     */
    public static ExtensionList<ComputerPinger> all() {
        return Hudson.getInstance().getExtensionList(ComputerPinger.class);
    }

    /**
     * Is this computer reachable via the given address?
     *
     * @param ia      The address to check.
     * @param timeout Timeout in seconds.
     */
    public static boolean checkIsReachable(InetAddress ia, int timeout) throws IOException {
        for (ComputerPinger pinger : ComputerPinger.all()) {
            try {
                if (pinger.isReachable(ia, timeout)) {
                    return true;
                }
            } catch (IOException e) {
                LOGGER.fine("Error checking reachability with " + pinger + ": " + e.getMessage());
            }
        }

        return false;
    }
    
    /**
     * Default pinger - use Java built-in functionality.  This doesn't always work,
     * a host may be reachable even if this returns false.
     */
    @Extension
    public static class BuiltInComputerPinger extends ComputerPinger {
        @Override
        public boolean isReachable(InetAddress ia, int timeout) throws IOException {
            return ia.isReachable(timeout * 1000);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ComputerPinger.class.getName());
}
