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

import hudson.model.AdministrativeMonitor;
import hudson.Extension;

/**
 * If {@link NodeMonitor} marks the node as offline, we'll show this to the admin to get their attention.
 *
 * <p>
 * This also allows them to disable the monitoring if they don't like it.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.301
 */
@Extension
public class MonitorMarkedNodeOffline extends AdministrativeMonitor {
    public boolean active = false;

    public boolean isActivated() {
        return active;
    }
}
