/*******************************************************************************
 *
 * Copyright (c) 2010, InfraDNA, Inc.
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

package hudson.slaves;

import java.io.IOException;
import java.util.logging.Logger;

import static hudson.util.TimeUnit2.*;
import static java.util.logging.Level.*;

/**
 * {@link RetentionStrategy} implementation for {@link AbstractCloudComputer} that terminates
 * it if it remains idle for X minutes.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.382
 */
public class CloudRetentionStrategy extends RetentionStrategy<AbstractCloudComputer> {
    private int idleMinutes;

    public CloudRetentionStrategy(int idleMinutes) {
        this.idleMinutes = idleMinutes;
    }

    public synchronized long check(AbstractCloudComputer c) {
        if (c.isIdle() && !disabled) {
            final long idleMilliseconds = System.currentTimeMillis() - c.getIdleStartMilliseconds();
            if (idleMilliseconds > MINUTES.toMillis(idleMinutes)) {
                LOGGER.info("Disconnecting "+c.getName());
                try {
                    c.getNode().terminate();
                } catch (InterruptedException e) {
                    LOGGER.log(WARNING,"Failed to terminate "+c.getName(),e);
                } catch (IOException e) {
                    LOGGER.log(WARNING,"Failed to terminate "+c.getName(),e);
                }
            }
        }
        return 1;
    }

    /**
     * Try to connect to it ASAP.
     */
    @Override
    public void start(AbstractCloudComputer c) {
        c.connect(false);
    }

    private static final Logger LOGGER = Logger.getLogger(CloudRetentionStrategy.class.getName());

    public static boolean disabled = Boolean.getBoolean(CloudRetentionStrategy.class.getName()+".disabled");
}
