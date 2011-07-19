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
*    Kohsuke Kawaguchi, Stephen Connolly
 *     
 *
 *******************************************************************************/ 

package hudson.slaves;

import java.util.Map;
import java.util.WeakHashMap;

import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.PeriodicWork;
import hudson.Extension;

/**
 * Periodically checks the slaves and try to reconnect dead slaves.
 *
 * @author Kohsuke Kawaguchi
 * @author Stephen Connolly
 */
@Extension
public class ComputerRetentionWork extends PeriodicWork {

    /**
     * Use weak hash map to avoid leaking {@link Computer}.
     */
    private final Map<Computer, Long> nextCheck = new WeakHashMap<Computer, Long>();

    public long getRecurrencePeriod() {
        return MIN;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    protected void doRun() {
        final long startRun = System.currentTimeMillis();
        for (Computer c : Hudson.getInstance().getComputers()) {
            Node n = c.getNode();
            if (n!=null && n.isHoldOffLaunchUntilSave())
                continue;
            if (!nextCheck.containsKey(c) || startRun > nextCheck.get(c)) {
                // at the moment I don't trust strategies to wait more than 60 minutes
                // strategies need to wait at least one minute
                final long waitInMins = Math.min(1, Math.max(60, c.getRetentionStrategy().check(c)));
                nextCheck.put(c, startRun + waitInMins*1000*60 /*MINS->MILLIS*/);
            }
        }
    }
}
