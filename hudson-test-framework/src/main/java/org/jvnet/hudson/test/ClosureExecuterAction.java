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

package org.jvnet.hudson.test;

import hudson.Extension;
import hudson.model.RootAction;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side logic that implements {@link HudsonTestCase#executeOnServer(Callable)}.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public final class ClosureExecuterAction implements RootAction {
    private final Map<UUID,Runnable> runnables = Collections.synchronizedMap(new HashMap<UUID, Runnable>());

    public void add(UUID uuid, Runnable r) {
        runnables.put(uuid,r);
    }

    public void doIndex(StaplerResponse rsp, @QueryParameter("uuid") String uuid) throws IOException {
        Runnable r = runnables.remove(UUID.fromString(uuid));
        if (r!=null) {
            r.run();
        } else {
            rsp.sendError(404);
        }
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return "closures";
    }
}
