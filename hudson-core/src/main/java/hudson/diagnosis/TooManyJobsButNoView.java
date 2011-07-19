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

import hudson.model.AdministrativeMonitor;
import hudson.model.Hudson;
import hudson.Extension;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

/**
 * If Hudson is run with a lot of jobs but no views, suggest the user that they can create views.
 *
 * <p>
 * I noticed at an user visit that some users didn't notice the '+' icon in the tab bar. 
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class TooManyJobsButNoView extends AdministrativeMonitor {
    public boolean isActivated() {
        Hudson h = Hudson.getInstance();
        return h.getViews().size()==1 && h.getItemMap().size()> THRESHOLD;
    }

    /**
     * Depending on whether the user said "yes" or "no", send him to the right place.
     */
    public void doAct(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if(req.hasParameter("no")) {
            disable(true);
            rsp.sendRedirect(req.getContextPath()+"/manage");
        } else {
            rsp.sendRedirect(req.getContextPath()+"/newView");
        }
    }

    public static final int THRESHOLD = 16;
}
