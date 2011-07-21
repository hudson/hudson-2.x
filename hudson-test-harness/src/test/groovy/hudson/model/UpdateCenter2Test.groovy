/**************************************************************************
#
# Copyright (C) 2004-2009 Oracle Corporation
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#         Kohsuke Kawaguchi
#
#**************************************************************************/ 
package hudson.model

import org.jvnet.hudson.test.HudsonTestCase
import hudson.model.UpdateCenter.DownloadJob.Success

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
public class UpdateCenter2Test extends HudsonTestCase {
    /**
     * Makes sure a plugin installs fine.
     */
    void testInstall() {
        UpdateSite.neverUpdate = false;
        createWebClient().goTo("/") // load the metadata
        def job = hudson.updateCenter.getPlugin("changelog-history").deploy().get(); // this seems like one of the smallest plugin
        println job.status;
        assertTrue(job.status instanceof Success)
    }
}
