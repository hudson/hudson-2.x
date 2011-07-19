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
 *    Kohsuke Kawaguchi
 *     
 *
 *******************************************************************************/ 

package hudson.lifecycle;

import hudson.model.Hudson;
import java.io.IOException;

/**
 * {@link Lifecycle} for Hudson installed as SMF service.
 *
 * @author Kohsuke Kawaguchi
 */
public class SolarisSMFLifecycle extends Lifecycle {
    /**
     * In SMF managed environment, just commit a suicide and the service will be restarted by SMF.
     */
    @Override
    public void restart() throws IOException, InterruptedException {
        Hudson h = Hudson.getInstance();
        if (h != null)
            h.cleanUp();
        System.exit(0);
    }
}
