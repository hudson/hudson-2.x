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

package hudson.scm;

import hudson.model.AbstractProject;
import hudson.model.Hudson;

/**
 * Maintains the automatically inferred {@link RepositoryBrowser} instance.
 *
 * <p>
 * To reduce the user's work, Hudson tries to infer applicable {@link RepositoryBrowser}
 * from configurations of other jobs. But this needs caution &mdash; for example,
 * such inferred {@link RepositoryBrowser} must be recalculated whenever
 * a job configuration changes somewhere.
 *
 * <p>
 * This class makes such tracking easy by hiding this logic.
 */
final class AutoBrowserHolder {
    private int cacheGeneration;
    private RepositoryBrowser cache;
    private SCM owner;

    public AutoBrowserHolder(SCM owner) {
        this.owner = owner;
    }

    public RepositoryBrowser get() {
        int g = owner.getDescriptor().generation;
        if(g!=cacheGeneration) {
            cacheGeneration = g;
            cache = infer();
        }
        return cache;
    }

    /**
     * Picks up a {@link RepositoryBrowser} that matches the
     * given {@link SCM} from existing other jobs.
     *
     * @return
     *      null if no applicable configuration was found.
     */
    private RepositoryBrowser infer() {
        for( AbstractProject p : Hudson.getInstance().getAllItems(AbstractProject.class) ) {
            SCM scm = p.getScm();
            if (scm!=null && scm.getClass()==owner.getClass() && scm.getBrowser()!=null &&
                    ((SCMDescriptor)scm.getDescriptor()).isBrowserReusable(scm,owner)) {
                return scm.getBrowser();
            }
        }
        return null;
    }
}
